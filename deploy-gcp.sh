#!/usr/bin/env bash
# ==============================================================================
# Paws & Claws Pet Store — GCP Automated Deployment Script
# Provisions Cloud SQL (db-f1-micro), GCS, Secret Manager, Artifact Registry,
# and deploys Spring Boot backend & React frontend to Cloud Run.
# ==============================================================================

set -euo pipefail

# Ensure homebrew and docker are in PATH
export PATH="/opt/homebrew/bin:/usr/local/bin:$HOME/.docker/bin:$PATH"

PROJECT_ID="${GCP_PROJECT_ID:-$(gcloud config get-value project 2>/dev/null || echo "")}"
REGION="${GCP_REGION:-us-central1}"
DB_INSTANCE="petstore-db"
DB_NAME="petstoredb"
DB_USER="postgres"
DB_PASSWORD="${DB_PASSWORD:-PetStore2026SecurePass!}"
JWT_SECRET="${JWT_SECRET:-dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWZvci1wZXQtc3RvcmUtdjEtZGV2ZWxvcG1lbnQtdGVzdC1rZXk=}"
BUCKET_NAME="${PROJECT_ID}-media"
REPO_NAME="petstore-repo"
SA_NAME="petstore-backend-sa"

echo "================================================================="
echo "🐾 Starting Deployment for Paws & Claws Pet Store"
echo "Project ID : ${PROJECT_ID}"
echo "Region     : ${REGION}"
echo "SQL Tier   : db-f1-micro (Option 1 - Low Cost)"
echo "================================================================="

if [ -z "${PROJECT_ID}" ]; then
  echo "❌ ERROR: No GCP project selected. Run: gcloud config set project <PROJECT_ID>"
  exit 1
fi

# ------------------------------------------------------------------------------
# 1. Verify Billing
# ------------------------------------------------------------------------------
echo "🔍 Checking GCP Billing status..."
BILLING_ENABLED=$(gcloud billing projects describe "${PROJECT_ID}" --format="value(billingEnabled)" 2>/dev/null || echo "false")
if [ "${BILLING_ENABLED}" != "true" ]; then
  echo "❌ ERROR: Billing is not enabled on project ${PROJECT_ID}."
  echo "👉 Please link an active billing account: https://console.cloud.google.com/billing/linkedaccount?project=${PROJECT_ID}"
  exit 1
fi
echo "✅ Billing is active on project ${PROJECT_ID}."

# ------------------------------------------------------------------------------
# 2. Enable Required APIs
# ------------------------------------------------------------------------------
echo "🔌 Enabling required GCP APIs..."
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  storage.googleapis.com \
  --project="${PROJECT_ID}"

# ------------------------------------------------------------------------------
# 3. Artifact Registry Setup
# ------------------------------------------------------------------------------
echo "📦 Setting up Artifact Registry..."
if ! gcloud artifacts repositories describe "${REPO_NAME}" --location="${REGION}" --project="${PROJECT_ID}" &>/dev/null; then
  gcloud artifacts repositories create "${REPO_NAME}" \
    --repository-format=docker \
    --location="${REGION}" \
    --description="Pet Store Docker images" \
    --project="${PROJECT_ID}"
fi
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# ------------------------------------------------------------------------------
# 4. Cloud Storage Bucket Setup
# ------------------------------------------------------------------------------
echo "🪣 Setting up Google Cloud Storage bucket..."
if ! gcloud storage buckets describe "gs://${BUCKET_NAME}" &>/dev/null; then
  gcloud storage buckets create "gs://${BUCKET_NAME}" \
    --location="${REGION}" \
    --project="${PROJECT_ID}" \
    --uniform-bucket-level-access
  
  # Allow public read access for catalog media
  gcloud storage buckets add-iam-policy-binding "gs://${BUCKET_NAME}" \
    --member="allUsers" \
    --role="roles/storage.objectViewer"
fi

# ------------------------------------------------------------------------------
# 5. Secret Manager Setup
# ------------------------------------------------------------------------------
echo "🔐 Setting up Secret Manager..."
if ! gcloud secrets describe petstore-db-password --project="${PROJECT_ID}" &>/dev/null; then
  printf "%s" "${DB_PASSWORD}" | gcloud secrets create petstore-db-password \
    --data-file=- \
    --project="${PROJECT_ID}"
fi

if ! gcloud secrets describe petstore-jwt-secret --project="${PROJECT_ID}" &>/dev/null; then
  printf "%s" "${JWT_SECRET}" | gcloud secrets create petstore-jwt-secret \
    --data-file=- \
    --project="${PROJECT_ID}"
fi

# ------------------------------------------------------------------------------
# 6. Service Account & IAM
# ------------------------------------------------------------------------------
echo "👤 Setting up Service Account & Permissions..."
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

if ! gcloud iam service-accounts describe "${SA_EMAIL}" --project="${PROJECT_ID}" &>/dev/null; then
  gcloud iam service-accounts create "${SA_NAME}" \
    --display-name="Pet Store Backend Service Account" \
    --project="${PROJECT_ID}"
fi

# Grant Cloud SQL Client
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/cloudsql.client" \
  --condition=None --quiet

# Grant Secret Manager Access
gcloud secrets add-iam-policy-binding petstore-db-password \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --project="${PROJECT_ID}" --quiet

gcloud secrets add-iam-policy-binding petstore-jwt-secret \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/secretmanager.secretAccessor" \
  --project="${PROJECT_ID}" --quiet

# Grant Cloud Storage Admin on media bucket
gcloud storage buckets add-iam-policy-binding "gs://${BUCKET_NAME}" \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/storage.objectAdmin"

# ------------------------------------------------------------------------------
# 7. Cloud SQL (PostgreSQL 16) Provisioning
# ------------------------------------------------------------------------------
echo "🗄️ Checking Cloud SQL instance (${DB_INSTANCE})..."
if ! gcloud sql instances describe "${DB_INSTANCE}" --project="${PROJECT_ID}" &>/dev/null; then
  echo "⏳ Creating Cloud SQL instance (tier: db-f1-micro). This takes about 4-6 minutes..."
  gcloud sql instances create "${DB_INSTANCE}" \
    --database-version=POSTGRES_16 \
    --tier=db-f1-micro \
    --region="${REGION}" \
    --storage-size=10GB \
    --storage-auto-increase \
    --project="${PROJECT_ID}"
fi

INSTANCE_CONNECTION_NAME=$(gcloud sql instances describe "${DB_INSTANCE}" --project="${PROJECT_ID}" --format="value(connectionName)")
echo "✅ Cloud SQL Connection: ${INSTANCE_CONNECTION_NAME}"

# Create Database if it does not exist
if ! gcloud sql databases describe "${DB_NAME}" --instance="${DB_INSTANCE}" --project="${PROJECT_ID}" &>/dev/null; then
  echo "Creating database ${DB_NAME}..."
  gcloud sql databases create "${DB_NAME}" --instance="${DB_INSTANCE}" --project="${PROJECT_ID}"
fi

# Set password for postgres user
echo "Setting database user password..."
gcloud sql users set-password "${DB_USER}" \
  --instance="${DB_INSTANCE}" \
  --password="${DB_PASSWORD}" \
  --project="${PROJECT_ID}"

# ------------------------------------------------------------------------------
# 8. Build & Deploy Backend (Spring Boot 3)
# ------------------------------------------------------------------------------
BACKEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/backend:latest"
echo "🔨 Building Backend container image (${BACKEND_IMAGE})..."
docker build --platform linux/amd64 -t "${BACKEND_IMAGE}" ./backend

echo "🚀 Pushing Backend image to Artifact Registry..."
docker push "${BACKEND_IMAGE}"

echo "🚢 Deploying Backend to Cloud Run..."
gcloud run deploy petstore-backend \
  --image="${BACKEND_IMAGE}" \
  --platform=managed \
  --region="${REGION}" \
  --project="${PROJECT_ID}" \
  --allow-unauthenticated \
  --service-account="${SA_EMAIL}" \
  --add-cloudsql-instances="${INSTANCE_CONNECTION_NAME}" \
  --set-env-vars="DATABASE_URL=jdbc:postgresql:///${DB_NAME}?host=/cloudsql/${INSTANCE_CONNECTION_NAME},DATABASE_USERNAME=${DB_USER},GCS_BUCKET=${BUCKET_NAME},STORAGE_BASE_URL=https://storage.googleapis.com/${BUCKET_NAME},STORAGE_PROVIDER=gcs,PORT=8080" \
  --set-secrets="DATABASE_PASSWORD=petstore-db-password:latest,JWT_SECRET=petstore-jwt-secret:latest" \
  --memory=1Gi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=5 \
  --port=8080

BACKEND_URL=$(gcloud run services describe petstore-backend --platform=managed --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)")
echo "✅ Backend Deployed: ${BACKEND_URL}"

# ------------------------------------------------------------------------------
# 9. Build & Deploy Frontend (React + Vite + Nginx)
# ------------------------------------------------------------------------------
FRONTEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/frontend:latest"
echo "🔨 Building Frontend container image pointing to API (${BACKEND_URL}/api)..."
docker build --platform linux/amd64 \
  --build-arg VITE_API_URL="${BACKEND_URL}/api" \
  -t "${FRONTEND_IMAGE}" ./frontend

echo "🚀 Pushing Frontend image to Artifact Registry..."
docker push "${FRONTEND_IMAGE}"

echo "🚢 Deploying Frontend to Cloud Run..."
gcloud run deploy petstore-frontend \
  --image="${FRONTEND_IMAGE}" \
  --platform=managed \
  --region="${REGION}" \
  --project="${PROJECT_ID}" \
  --allow-unauthenticated \
  --port=80 \
  --memory=256Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=5

FRONTEND_URL=$(gcloud run services describe petstore-frontend --platform=managed --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)")
echo "✅ Frontend Deployed: ${FRONTEND_URL}"

# ------------------------------------------------------------------------------
# 10. Update Backend CORS to Allow Frontend URL
# ------------------------------------------------------------------------------
echo "🔄 Updating Backend CORS to allow ${FRONTEND_URL}..."
gcloud run services update petstore-backend \
  --platform=managed \
  --region="${REGION}" \
  --project="${PROJECT_ID}" \
  --update-env-vars="CORS_ALLOWED_ORIGINS=${FRONTEND_URL},http://localhost:5173,http://localhost:3000"

echo "================================================================="
echo "🎉 DEPLOYMENT COMPLETE!"
echo "🌐 Frontend URL  : ${FRONTEND_URL}"
echo "🔌 Backend API    : ${BACKEND_URL}"
echo "📖 Swagger Docs   : ${BACKEND_URL}/swagger-ui/index.html"
echo "🗄️ Cloud SQL     : ${INSTANCE_CONNECTION_NAME}"
echo "🪣 Storage Bucket : https://storage.googleapis.com/${BUCKET_NAME}"
echo "================================================================="
