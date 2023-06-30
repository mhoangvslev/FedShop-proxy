FROM maven:3.9.3-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace
COPY . /workspace/
COPY settings.xml /usr/share/maven/conf/
RUN mvn install dependency:copy-dependencies package 
CMD []
# FROM eclipse-temurin:17-jdk-alpine
# WORKDIR /fedshop-proxy
# COPY --from=builder /workspace/target/FedShop-proxy-1.0-SNAPSHOT.jar /fedshop-proxy
# ENTRYPOINT ["sh", "-c", "java -jar /fedshop-proxy/FedShop-proxy-1.0-SNAPSHOT.jar"]
