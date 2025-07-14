#!/bin/bash

# Glavna skripta za primenu svih K-fold poslova na Kubernetes klasteru 

NAMESPACE="aleksandarm"
CONFIG_PATH="./config/kfold-configmap.yaml"
FOLD_TEMPLATE="./jobs/fold-job-template.yaml"
AGGREGATE_JOB="./jobs/aggregate-job.yaml"

# Primena ConfigMap
kubectl apply -f "$CONFIG_PATH" -n $NAMESPACE

# Kreiranje 10-fold job-a
for i in {0..9}; do
  sed "s/{{FOLD_INDEX}}/$i/g" "$FOLD_TEMPLATE" | kubectl apply -f - -n $NAMESPACE
done

# Čekanje da se svi fold poslovi završe
# Očekuje se da svaki posao bude završen u roku od 600 sekundi
echo "Waiting for all fold jobs to complete..."
kubectl wait --for=condition=complete job -n $NAMESPACE --timeout=600s --all

# Pokretanje agregacionog posla koji prikuplja sve rezultate
echo "Applying aggregate job..."
kubectl apply -f "$AGGREGATE_JOB" -n $NAMESPACE
