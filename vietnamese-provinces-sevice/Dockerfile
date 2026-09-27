# ==========================================
# Stage 1: Build + Unit Test (JDK 25)
# ==========================================
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

# ==========================================
# Stage 2: Runtime (JRE 25 Alpine + non-root user)
# ==========================================
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -g 1001 -S spring && \
  adduser -u 1001 -S spring -G spring

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R spring:spring /app

USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]