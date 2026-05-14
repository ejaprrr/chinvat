.PHONY: help backend-build backend-test backend-fmt backend-fmt-check backend-run-local backend-run-dev \
        infra-config-dev infra-config-prod dev-up dev-down infra-dev-logs \
        infra-prod-up infra-prod-down infra-prod-logs infra-docker-build \
        db-migrate db-seed db-reset redis-flush dev-restart-backend

help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

backend-build: ## Build backend modules
	$(MAKE) -C backend build

backend-test: ## Run backend tests
	$(MAKE) -C backend test

backend-fmt: ## Format backend Java code
	$(MAKE) -C backend fmt

backend-fmt-check: ## Verify backend Java formatting
	$(MAKE) -C backend fmt-check

backend-run-local: ## Run backend with local profile
	$(MAKE) -C backend run-local

backend-run-dev: ## Run backend with dev profile
	$(MAKE) -C backend run-dev

infra-config-dev: ## Render development infrastructure config
	$(MAKE) -C infra config-dev

infra-config-prod: ## Render production infrastructure config
	$(MAKE) -C infra config-prod

dev-up: ## Start development infrastructure stack
	$(MAKE) -C infra dev-up

dev-down: ## Stop development infrastructure stack
	$(MAKE) -C infra dev-down

infra-dev-logs: ## Tail development infrastructure logs
	$(MAKE) -C infra dev-logs

infra-prod-up: ## Start production infrastructure stack
	$(MAKE) -C infra prod-up

infra-prod-down: ## Stop production infrastructure stack
	$(MAKE) -C infra prod-down

infra-prod-logs: ## Tail production infrastructure logs
	$(MAKE) -C infra prod-logs

infra-docker-build: ## Build the production backend image
	$(MAKE) -C infra docker-build

# ── Database helpers ──────────────────────────────────────────────────────────

db-migrate: ## Run Prisma migrations against the dev database
	$(MAKE) -C infra db-migrate

db-seed: ## Seed the dev database
	$(MAKE) -C infra db-seed

db-reset: ## DEV ONLY – drop schema, re-migrate and re-seed
	$(MAKE) -C infra db-reset

redis-flush: ## DEV ONLY – flush all Redis data (both instances)
	$(MAKE) -C infra redis-flush

dev-restart-backend: ## Flush Redis and restart backend (use after config changes)
	$(MAKE) -C infra dev-restart-backend
