.PHONY: help config-dev config-prod dev-up dev-down dev-logs prod-up prod-down prod-logs docker-build \
        db-migrate db-seed db-reset

COMPOSE_DEV  = docker compose --env-file docker/.env.dev.example  -f docker/compose.dev.yml
COMPOSE_PROD = docker compose --env-file docker/.env.prod.example -f docker/compose.prod.yml

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

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
	docker build -f backend/Dockerfile --target prod -t chinvat-backend:local ..

# ── Database helpers ──────────────────────────────────────────────────────────

db-migrate: ## Run Prisma migrations against the dev database
	$(COMPOSE_DEV) run --rm db-migrate

db-seed: ## Seed the dev database (idempotent via audit-event guard)
	$(COMPOSE_DEV) run --rm db-seed

db-reset: ## DEV ONLY – drop schema, re-migrate and re-seed
	@echo "\033[31mWARNING: This will DESTROY all data in the dev database!\033[0m"
	@echo "Set ALLOW_DB_RESET=true to confirm."
	ALLOW_DB_RESET=true $(COMPOSE_DEV) run --rm db-reset
	$(COMPOSE_DEV) run --rm db-migrate
	$(COMPOSE_DEV) run --rm db-seed
