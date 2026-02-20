#!/usr/bin/env bash
set -e

PROJECT_ID="grpc-boundary-lab"
BILLING_ACCOUNT="013DA2-4E03AA-7141D8"
REPO="AndySchubert/grpc-boundary-lab"
REGION="europe-west1"
SA_NAME="github-actions-deploy"
POOL_NAME="github-pool"
PROVIDER_NAME="github-provider"
BUCKET_NAME="${PROJECT_ID}-tf-state"

echo "Creating Project ${PROJECT_ID}..."
gcloud projects create ${PROJECT_ID} || true

echo "Linking Billing..."
gcloud beta billing projects link ${PROJECT_ID} --billing-account=${BILLING_ACCOUNT}

echo "Enabling APIs..."
gcloud services enable \
  iam.googleapis.com \
  cloudresourcemanager.googleapis.com \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  iamcredentials.googleapis.com \
  sts.googleapis.com \
  --project=${PROJECT_ID}

PROJECT_NUMBER=$(gcloud projects describe ${PROJECT_ID} --format="value(projectNumber)")

echo "Creating Service Account..."
gcloud iam service-accounts create ${SA_NAME} \
  --description="Service account for GitHub Actions deployment" \
  --display-name="GitHub Actions Deploy" \
  --project=${PROJECT_ID} || true

SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "Granting Roles to Service Account..."
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/editor"

gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/resourcemanager.projectIamAdmin"

gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding ${PROJECT_ID} \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"

echo "Setting up Workload Identity Federation..."
gcloud iam workload-identity-pools create ${POOL_NAME} \
  --location="global" \
  --project=${PROJECT_ID} || true

gcloud iam workload-identity-pools providers create-oidc ${PROVIDER_NAME} \
  --workload-identity-pool=${POOL_NAME} \
  --location="global" \
  --project=${PROJECT_ID} \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-condition="assertion.repository == '${REPO}'" || true

echo "Binding SA to Workload Identity Pool..."
gcloud iam service-accounts add-iam-policy-binding ${SA_EMAIL} \
  --project=${PROJECT_ID} \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/${POOL_NAME}/attribute.repository/${REPO}"

echo "Creating Terraform State Bucket..."
gcloud storage buckets create gs://${BUCKET_NAME} \
  --project=${PROJECT_ID} \
  --location=${REGION} || true

echo "=================="
echo "GCP Setup Complete!"
echo "Project Number: ${PROJECT_NUMBER}"
echo "Use this Provider string in Github Actions:"
echo "projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/${POOL_NAME}/providers/${PROVIDER_NAME}"
