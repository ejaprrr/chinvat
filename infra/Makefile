.PHONY: help config-dev config-prod dev-up dev-down dev-logs prod-up prod-down prod-logs docker-build

COMPOSE_DEV = docker compose -f docker/compose.dev.yml
COMPOSE_PROD = docker compose -f docker/compose.prod.yml

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

config-dev: ## Render development Compose configuration
	$(COMPOSE_DEV) config

config-prod: ## Render production Compose configuration
	$(COMPOSE_PROD) config

dev-up: ## Start the development stack
	$(COMPOSE_DEV) up --build -d

dev-down: ## Stop the development stack
	$(COMPOSE_DEV) down --remove-orphans

dev-logs: ## Tail development logs
	$(COMPOSE_DEV) logs -f

prod-up: ## Start the production stack
	$(COMPOSE_PROD) up --build -d

prod-down: ## Stop the production stack
	$(COMPOSE_PROD) down --remove-orphans

prod-logs: ## Tail production logs
	$(COMPOSE_PROD) logs -f

docker-build: ## Build the production backend image
	docker build -f docker/app/Dockerfile --target prod -t chinvat-backend:local ..

