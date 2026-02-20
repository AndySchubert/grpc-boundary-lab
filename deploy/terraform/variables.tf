variable "project_id" {
  type        = string
  description = "GCP Project ID"
}

variable "region" {
  type        = string
  default     = "europe-west1"
  description = "GCP Region"
}

variable "artifact_repo_id" {
  type        = string
  description = "Artifact Registry repository ID (name)"
}

variable "image_tag_backend" {
  type        = string
  default     = "latest"
  description = "Backend Docker image tag"
}

variable "image_tag_gateway" {
  type        = string
  default     = "latest"
  description = "Gateway Docker image tag"
}
