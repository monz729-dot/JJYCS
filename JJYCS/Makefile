# YCS LMS Makefile
.PHONY: help build test deploy clean

# Variables
DOCKER_REGISTRY ?= ghcr.io
DOCKER_IMAGE ?= ycs-lms/backend
VERSION ?= latest
ENV ?= development

# Colors
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m

help: ## Show this help message
	@echo "YCS LMS Build & Deployment Commands"
	@echo "===================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# Development
dev: ## Start development environment
	@echo "$(YELLOW)Starting development environment...$(NC)"
	docker-compose up -d mysql redis
	cd backend && ./mvnw spring-boot:run -Dspring.profiles.active=dev

dev-full: ## Start full development stack
	@echo "$(YELLOW)Starting full development stack...$(NC)"
	docker-compose up -d

logs: ## Show application logs
	docker-compose logs -f backend

# Building
build: ## Build backend application
	@echo "$(YELLOW)Building backend application...$(NC)"
	cd backend && ./mvnw clean package -DskipTests

build-docker: ## Build Docker image
	@echo "$(YELLOW)Building Docker image...$(NC)"
	docker build -t $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(VERSION) ./backend

# Testing
test: ## Run all tests
	@echo "$(YELLOW)Running tests...$(NC)"
	cd backend && ./mvnw test

test-integration: ## Run integration tests
	@echo "$(YELLOW)Running integration tests...$(NC)"
	docker-compose up -d mysql redis
	sleep 10
	cd backend && ./mvnw test -Dspring.profiles.active=test
	docker-compose down

smoke-test: ## Run smoke tests
	@echo "$(YELLOW)Running smoke tests...$(NC)"
	./scripts/smoke-test.sh http://localhost:8080

health-check: ## Run health checks
	@echo "$(YELLOW)Running health checks...$(NC)"
	./scripts/health-check.sh http://localhost:8080

# Database
db-backup: ## Create database backup
	@echo "$(YELLOW)Creating database backup...$(NC)"
	./scripts/backup.sh

db-restore: ## Restore database from backup
	@echo "$(YELLOW)Restoring database...$(NC)"
	@read -p "Enter backup file path: " backup_file; \
	./scripts/restore.sh $$backup_file

db-migrate: ## Run database migrations
	@echo "$(YELLOW)Running database migrations...$(NC)"
	cd backend && ./mvnw flyway:migrate

# Docker Compose
up: ## Start all services
	@echo "$(GREEN)Starting all services...$(NC)"
	docker-compose up -d

down: ## Stop all services
	@echo "$(RED)Stopping all services...$(NC)"
	docker-compose down

restart: ## Restart all services
	@echo "$(YELLOW)Restarting all services...$(NC)"
	docker-compose restart

ps: ## Show service status
	docker-compose ps

# Kubernetes
k8s-deploy: ## Deploy to Kubernetes
	@echo "$(YELLOW)Deploying to Kubernetes ($(ENV))...$(NC)"
	kubectl apply -f k8s/namespace.yaml
	kubectl apply -f k8s/configmap.yaml
	kubectl apply -f k8s/secret.yaml
	kubectl apply -f k8s/mysql-deployment.yaml
	kubectl apply -f k8s/redis-deployment.yaml
	kubectl apply -f k8s/backend-deployment.yaml
	kubectl apply -f k8s/ingress.yaml
	kubectl apply -f k8s/hpa.yaml

k8s-delete: ## Delete from Kubernetes
	@echo "$(RED)Deleting from Kubernetes...$(NC)"
	kubectl delete -f k8s/

k8s-status: ## Show Kubernetes status
	kubectl get all -n ycs-lms

k8s-logs: ## Show Kubernetes logs
	kubectl logs -f deployment/backend -n ycs-lms

# Monitoring
monitor-start: ## Start monitoring stack
	@echo "$(YELLOW)Starting monitoring stack...$(NC)"
	docker-compose up -d prometheus grafana

monitor-stop: ## Stop monitoring stack
	@echo "$(RED)Stopping monitoring stack...$(NC)"
	docker-compose stop prometheus grafana

grafana: ## Open Grafana dashboard
	@echo "$(GREEN)Opening Grafana...$(NC)"
	open http://localhost:3000

prometheus: ## Open Prometheus dashboard
	@echo "$(GREEN)Opening Prometheus...$(NC)"
	open http://localhost:9090

# Cleanup
clean: ## Clean build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	cd backend && ./mvnw clean
	rm -rf backend/target
	rm -rf backend/logs
	rm -rf logs/

clean-docker: ## Clean Docker resources
	@echo "$(YELLOW)Cleaning Docker resources...$(NC)"
	docker-compose down -v
	docker system prune -f

clean-all: clean clean-docker ## Clean everything
	@echo "$(GREEN)All cleaned up!$(NC)"

# Production
deploy-staging: build build-docker ## Deploy to staging
	@echo "$(YELLOW)Deploying to staging...$(NC)"
	docker push $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(VERSION)
	kubectl set image deployment/backend backend=$(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(VERSION) -n ycs-lms-staging

deploy-production: ## Deploy to production
	@echo "$(RED)Deploying to production...$(NC)"
	@read -p "Are you sure? (y/N): " confirm; \
	if [ "$$confirm" = "y" ]; then \
		docker push $(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(VERSION); \
		kubectl set image deployment/backend backend=$(DOCKER_REGISTRY)/$(DOCKER_IMAGE):$(VERSION) -n ycs-lms-production; \
		echo "$(GREEN)Deployed to production!$(NC)"; \
	else \
		echo "$(YELLOW)Deployment cancelled.$(NC)"; \
	fi

rollback: ## Rollback deployment
	@echo "$(YELLOW)Rolling back deployment...$(NC)"
	kubectl rollout undo deployment/backend -n ycs-lms-$(ENV)

# Info
version: ## Show version information
	@echo "YCS LMS Version Information"
	@echo "=========================="
	@echo "Backend Version: $(VERSION)"
	@echo "Java Version: $(shell java -version 2>&1 | head -n 1)"
	@echo "Maven Version: $(shell cd backend && ./mvnw -v | head -n 1)"
	@echo "Docker Version: $(shell docker --version)"
	@echo "Kubectl Version: $(shell kubectl version --client --short 2>/dev/null || echo "Not installed")"

status: ## Show overall system status
	@echo "$(YELLOW)System Status$(NC)"
	@echo "============="
	@docker-compose ps
	@echo ""
	@echo "$(YELLOW)Health Check$(NC)"
	@echo "============"
	@curl -s http://localhost:8080/health/simple | jq . || echo "Backend not running"