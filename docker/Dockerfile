FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src
COPY --from=frontend-build /app/frontend/dist ./frontend/dist
WORKDIR /app/backend
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/biomed-digital-system-1.0.0.jar app.jar
EXPOSE 8088
CMD ["java", "-jar", "app.jar"]
