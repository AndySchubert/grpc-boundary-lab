terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# Artifact Registry for Docker images
resource "google_artifact_registry_repository" "repo" {
  location      = var.region
  repository_id = var.artifact_repo_id
  format        = "DOCKER"
  description   = "Repo for grpc-boundary-lab images"

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_cloud_run_v2_service" "api" {
  name     = "grpc-boundary-lab"
  location = var.region
  project  = var.project_id
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    # Backend container (internal, only reachable via localhost locally)
    containers {
      name  = "backend"
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo_id}/backend:${var.image_tag_backend}"
    }

    # Gateway container (ingress)
    containers {
      name  = "gateway"
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo_id}/gateway:${var.image_tag_gateway}"

      ports {
        container_port = 50052
      }

      env {
        name  = "BACKEND_HOST"
        value = "localhost" # Connect to sidecar
      }

      env {
        name  = "BACKEND_PORT"
        value = "50051"
      }
    }
  }
}

resource "google_cloud_run_v2_service_iam_member" "public_access" {
  name     = google_cloud_run_v2_service.api.name
  location = google_cloud_run_v2_service.api.location
  project  = google_cloud_run_v2_service.api.project
  role     = "roles/run.invoker"
  member   = "allUsers"
}

output "cloud_run_url" {
  value = google_cloud_run_v2_service.api.uri
}
