.PHONY: help build test fmt fmt-check run-local run-dev clean

## Show help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

build: ## Build all modules
	./mvnw package -DskipTests -q

test: ## Run all tests
	./mvnw test

fmt: ## Format Java code (Google Java Style)
	./mvnw spotless:apply

fmt-check: ## Verify Java formatting
	./mvnw spotless:check

run-local: ## Run app with local profile (H2)
	./mvnw -pl app spring-boot:run -Dspring-boot.run.profiles=local

run-dev: ## Run app with dev profile
	./mvnw -pl app spring-boot:run -Dspring-boot.run.profiles=dev


clean: ## Remove build artifacts
	./mvnw clean -q

