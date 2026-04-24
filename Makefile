.PHONY: help backend-build backend-test backend-fmt backend-fmt-check backend-run-local backend-run-dev infra-config-dev infra-config-prod infra-dev-up infra-dev-down infra-dev-logs infra-prod-up infra-prod-down infra-prod-logs

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

infra-dev-up: ## Start development infrastructure stack
	$(MAKE) -C infra dev-up

infra-dev-down: ## Stop development infrastructure stack
	$(MAKE) -C infra dev-down

infra-dev-logs: ## Tail development infrastructure logs
	$(MAKE) -C infra dev-logs

infra-prod-up: ## Start production infrastructure stack
	$(MAKE) -C infra prod-up

infra-prod-down: ## Stop production infrastructure stack
	$(MAKE) -C infra prod-down

infra-prod-logs: ## Tail production infrastructure logs
	$(MAKE) -C infra prod-logs

