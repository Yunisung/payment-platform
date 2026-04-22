#!/bin/bash
# 쿠버네티스 배포 스크립트 (Docker Desktop 로컬 클러스터용)
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="localhost:5000"

step() { echo -e "\n${GREEN}=== $1 ===${NC}"; }
info() { echo -e "${YELLOW}  > $1${NC}"; }

# -------------------------------------------------------------------
# 옵션 파싱
# -------------------------------------------------------------------
BUILD=true
if [[ "$1" == "--no-build" ]]; then
  BUILD=false
fi

# -------------------------------------------------------------------
# 1. 로컬 레지스트리 확인 / 시작
# -------------------------------------------------------------------
step "로컬 레지스트리 확인"
if ! docker inspect local-registry &>/dev/null; then
  info "로컬 레지스트리 시작 (localhost:5000)"
  docker run -d -p 5000:5000 --restart=always --name local-registry registry:2
fi

# -------------------------------------------------------------------
# 2. Docker 이미지 빌드 및 푸시
# -------------------------------------------------------------------
if [ "$BUILD" = true ]; then
  step "Docker 이미지 빌드 & 레지스트리 푸시"

  info "api-server 빌드"
  docker build -f api-server/Dockerfile -t payment/api-server:latest .
  docker tag payment/api-server:latest $REGISTRY/payment/api-server:latest
  docker push $REGISTRY/payment/api-server:latest

  info "settlement-service 빌드"
  docker build -f settlement-service/Dockerfile -t payment/settlement-service:latest .
  docker tag payment/settlement-service:latest $REGISTRY/payment/settlement-service:latest
  docker push $REGISTRY/payment/settlement-service:latest

  info "batch 빌드"
  docker build -f batch/Dockerfile -t payment/batch:latest .
  docker tag payment/batch:latest $REGISTRY/payment/batch:latest
  docker push $REGISTRY/payment/batch:latest
fi

# -------------------------------------------------------------------
# 3. ConfigMap / Secret
# -------------------------------------------------------------------
step "ConfigMap & Secret 적용"
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml

# -------------------------------------------------------------------
# 4. 인프라 (Zookeeper → Kafka → MySQL → Redis)
# -------------------------------------------------------------------
step "인프라 배포"
info "Zookeeper"
kubectl apply -f k8s/zookeeper/

info "Kafka"
kubectl apply -f k8s/kafka/

info "MySQL"
kubectl apply -f k8s/mysql/

info "Redis"
kubectl apply -f k8s/redis/

# -------------------------------------------------------------------
# 5. 인프라 준비 대기
# -------------------------------------------------------------------
step "인프라 준비 대기"
info "Zookeeper 준비 대기..."
kubectl rollout status deployment/zookeeper --timeout=120s

info "Kafka 준비 대기..."
kubectl rollout status deployment/kafka --timeout=180s

info "MySQL 준비 대기..."
kubectl rollout status statefulset/mysql --timeout=180s

info "Redis 준비 대기..."
kubectl rollout status deployment/redis --timeout=60s

# -------------------------------------------------------------------
# 6. 애플리케이션 배포
# -------------------------------------------------------------------
step "애플리케이션 배포"
info "api-server"
kubectl apply -f k8s/api-server/

info "settlement-service"
kubectl apply -f k8s/settlement-service/

info "batch"
kubectl apply -f k8s/batch/

# -------------------------------------------------------------------
# 7. 배포 상태 확인
# -------------------------------------------------------------------
step "배포 완료"
echo ""
kubectl get pods -o wide
echo ""
echo "api-server 접근: http://localhost:30080"
