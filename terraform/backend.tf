terraform {
  backend "gcs" {
    bucket  = "grpc-boundary-lab-tf-state"
    prefix  = "terraform/state"
  }
}
