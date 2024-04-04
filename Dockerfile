FROM --platform=${BUILDPLATFORM} maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace
COPY . /workspace/
# COPY settings.xml /usr/share/maven/conf/
# RUN mvn install dependency:copy-dependencies package -DskipTests
RUN mvn clean package spring-boot:repackage -DskipTests

FROM --platform=${TARGETPLATFORM} eclipse-temurin:21.0.2_13-jre-alpine
WORKDIR /fedshop-proxy
COPY --from=builder /workspace/target/FedShop-proxy-1.0-SNAPSHOT.jar /fedshop-proxy
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=8080 /fedshop-proxy/FedShop-proxy-1.0-SNAPSHOT.jar"]


