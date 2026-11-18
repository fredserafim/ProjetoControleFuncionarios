FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/shiftsync-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java","-jar", "shiftsync-0.0.1-SNAPSHOT.jar"]