# Makefile — jednoduchý dev workflow
# Použití: make <target>

.PHONY: help build test run-local run-dev run-prod docker-dev docker-prod clean

## Zobrazí nápovědu
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

## Zkompiluje celý projekt
build: ## Build všech modulů
	./mvnw package -DskipTests -q

## Spustí všechny testy
test: ## Spustí unit + integration testy
	./mvnw test

## Spustí app lokálně s H2 (bez Dockeru)
run-local: ## Spring Boot run s local profilem (H2)
	./mvnw -pl app spring-boot:run -Dspring-boot.run.profiles=local

## Spustí app s dev profilem (vyžaduje Postgres)
run-dev: ## Spring Boot run s dev profilem
	./mvnw -pl app spring-boot:run -Dspring-boot.run.profiles=dev

## Docker dev (s live-reload a debuggerem na :5005)
docker-dev: ## Docker Compose dev stack (app + postgres)
	docker compose -f docker-compose.dev.yml up --build

## Docker prod
docker-prod: ## Docker Compose prod stack
	docker compose -f docker-compose.prod.yml up -d --build

## Zastaví Docker prod
docker-prod-down: ## Zastaví prod stack
	docker compose -f docker-compose.prod.yml down

## Smaže build artefakty
clean: ## Maven clean
	./mvnw clean -q

