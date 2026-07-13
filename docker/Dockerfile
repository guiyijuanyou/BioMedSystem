# ---- Stage 1: Build frontend ----
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci && npm cache clean --force
COPY frontend ./
RUN npm run build

# ---- Stage 2: Download Maven dependencies (layer caching) ----
FROM maven:3.9.9-eclipse-temurin-21 AS deps
WORKDIR /app
COPY backend/pom.xml ./backend/pom.xml
COPY backend/.mvn ./backend/.mvn
WORKDIR /app/backend
RUN mvn dependency:go-offline -B

# ---- Stage 3: Build backend ----
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
COPY --from=frontend-build /app/frontend/dist ./frontend/dist
WORKDIR /app/backend
RUN mvn -Pprod -DskipTests -B package

# ---- Stage 4: Runtime ----
FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser -m -d /app appuser
WORKDIR /app

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseZGC -XX:MaxMetaspaceSize=256m"

COPY --from=backend-build /app/backend/target/biomed-digital-system-1.0.0.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8088

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
    CMD wget -qO- http://localhost:8088/api/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
