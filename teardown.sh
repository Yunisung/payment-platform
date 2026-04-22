#!/bin/bash
# 쿠버네티스 리소스 전체 제거
set -e

echo "=== 애플리케이션 제거 ==="
kubectl delete -f k8s/api-server/ --ignore-not-found
kubectl delete -f k8s/settlement-service/ --ignore-not-found
kubectl delete -f k8s/batch/ --ignore-not-found

echo "=== 인프라 제거 ==="
kubectl delete -f k8s/kafka/ --ignore-not-found
kubectl delete -f k8s/zookeeper/ --ignore-not-found
kubectl delete -f k8s/redis/ --ignore-not-found
kubectl delete -f k8s/mysql/ --ignore-not-found

echo "=== PVC 제거 (MySQL 데이터 포함) ==="
kubectl delete pvc -l app=mysql --ignore-not-found

echo "=== ConfigMap & Secret 제거 ==="
kubectl delete -f k8s/configmap.yaml --ignore-not-found
kubectl delete -f k8s/secret.yaml --ignore-not-found

echo "=== 완료 ==="
kubectl get pods
