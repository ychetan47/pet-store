#!/usr/bin/env bash
# ==============================================================================
# Paws & Claws Pet Store — GCP Strategy A Teardown Script
# Deletes all Cloud Run microservices, Cloud SQL database, and Kafka VM
# ==============================================================================

set -euo pipefail

PROJECT_ID="${GCP_PROJECT_ID:-$(gcloud config get-value project 2>/dev/null || echo "petstore-app-1788943786")}"
REGION="${GCP_REGION:-us-central1}"
ZONE="${GCP_ZONE:-us-central1-a}"

echo "================================================================="
echo "🗑️  Tearing down Strategy A resources in project: ${PROJECT_ID}"
echo "================================================================="

# 1. Delete Cloud Run Services
SERVICES=(
  "petstore-customer-frontend"
  "petstore-admin-frontend"
  "petstore-api-gateway"
  "petstore-user-service"
  "petstore-catalog-service"
  "petstore-inventory-service"
  "petstore-order-service"
  "petstore-notification-service"
)

for svc in "${SERVICES[@]}"; do
  if gcloud run services describe "${svc}" --region="${REGION}" --project="${PROJECT_ID}" >/dev/null 2>&1; then
    echo "Deleting Cloud Run service: ${svc}..."
    gcloud run services delete "${svc}" --region="${REGION}" --project="${PROJECT_ID}" --quiet
  else
    echo "Cloud Run service ${svc} not found, skipping."
  fi
done

# 2. Delete Kafka VM
if gcloud compute instances describe petstore-kafka --zone="${ZONE}" --project="${PROJECT_ID}" >/dev/null 2>&1; then
  echo "Deleting Kafka VM: petstore-kafka..."
  gcloud compute instances delete petstore-kafka --zone="${ZONE}" --project="${PROJECT_ID}" --quiet
else
  echo "Kafka VM petstore-kafka not found, skipping."
fi

# 3. Delete Cloud SQL Instance
if gcloud sql instances describe petstore-db --project="${PROJECT_ID}" >/dev/null 2>&1; then
  echo "Deleting Cloud SQL instance: petstore-db..."
  gcloud sql instances delete petstore-db --project="${PROJECT_ID}" --quiet
else
  echo "Cloud SQL instance petstore-db not found, skipping."
fi

echo "================================================================="
echo "✅ Strategy A teardown complete! No ongoing VM or DB costs remain."
echo "================================================================="
