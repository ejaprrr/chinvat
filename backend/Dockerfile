FROM eclipse-temurin:21-jdk-alpine AS base
WORKDIR /workspace/backend
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
COPY backend/common/pom.xml common/pom.xml
COPY backend/modules/users/pom.xml modules/users/pom.xml
COPY backend/modules/rbac/pom.xml modules/rbac/pom.xml
COPY backend/modules/auth/pom.xml modules/auth/pom.xml
COPY backend/app/pom.xml app/pom.xml
RUN ./mvnw dependency:go-offline -q

FROM base AS test
COPY backend/modules/users/src modules/users/src
COPY backend/modules/rbac/src modules/rbac/src
COPY backend/modules/auth/src modules/auth/src
COPY backend/app/src app/src
RUN ./mvnw test --no-transfer-progress

FROM test AS build
RUN ./mvnw -pl app -am package -DskipTests -q

RUN java -Djarmode=layertools \
    -jar app/target/*.jar extract \
    --destination app/target/extracted

FROM eclipse-temurin:21-jdk-alpine AS dev
WORKDIR /app

RUN addgroup -S chinvat && adduser -S chinvat -G chinvat
USER chinvat

COPY --from=build /workspace/backend/app/target/extracted/dependencies/ ./
COPY --from=build /workspace/backend/app/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/backend/app/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/backend/app/target/extracted/application/ ./

ENV SPRING_PROFILES_ACTIVE=local
ENV SERVER_PORT=8080
EXPOSE 8080
EXPOSE 5005

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "org.springframework.boot.loader.launch.JarLauncher"]

FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app

RUN addgroup -S chinvat && adduser -S chinvat -G chinvat
USER chinvat

COPY --from=build /workspace/backend/app/target/extracted/dependencies/ ./
COPY --from=build /workspace/backend/app/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/backend/app/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/backend/app/target/extracted/application/ ./

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]

