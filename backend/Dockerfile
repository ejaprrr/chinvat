# =============================================================================
# STAGE 1: base — základ se správnou Java verzí
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS base
WORKDIR /workspace
# Dependence cache: zkopíruj pomy a stáhni závislosti odděleně od zdrojáků
# => rebuild zdrojáků nevymaže Maven cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/pom.xml common/pom.xml
COPY app/pom.xml app/pom.xml
RUN ./mvnw dependency:go-offline -q

# =============================================================================
# STAGE 2: build — zkompiluj a vytvoř JAR
# =============================================================================
FROM base AS build
COPY common/src common/src
COPY app/src app/src
RUN ./mvnw -pl common,app package -DskipTests -q

# Rozbal JAR do vrstev pro efektivní Docker cache (Spring Boot layered JAR)
RUN java -Djarmode=layertools \
    -jar app/target/*.jar extract \
    --destination app/target/extracted

# =============================================================================
# STAGE 3: dev — vývoj s plným JDK (debugger, devtools)
# =============================================================================
FROM eclipse-temurin:21-jdk-alpine AS dev
WORKDIR /app

# Přidej non-root uživatele
RUN addgroup -S chinvat && adduser -S chinvat -G chinvat
USER chinvat

COPY --from=build /workspace/app/target/extracted/dependencies/ ./
COPY --from=build /workspace/app/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/app/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/app/target/extracted/application/ ./

ENV SPRING_PROFILES_ACTIVE=local
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "org.springframework.boot.loader.launch.JarLauncher"]

# =============================================================================
# STAGE 4: prod — minimální runtime JRE, žádný debugger
# =============================================================================
FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app

RUN addgroup -S chinvat && adduser -S chinvat -G chinvat
USER chinvat

COPY --from=build /workspace/app/target/extracted/dependencies/ ./
COPY --from=build /workspace/app/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/app/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/app/target/extracted/application/ ./

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
EXPOSE 8080

# Docker healthcheck — volá náš /api/v1/health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]

