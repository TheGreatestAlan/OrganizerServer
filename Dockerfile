#
# Build stage - compile the app
#
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests package

#
# Runtime stage - minimal image with the built JAR
#
FROM eclipse-temurin:17-jre-jammy
WORKDIR /usr/local/lib
VOLUME ["/data"]
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip \
  && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/target/organizerserver-1.0-SNAPSHOT.jar /usr/local/lib/organizer.jar
COPY dev/configuration.yml /usr/local/lib/configuration.yml
COPY mcp_server/requirements.txt /usr/local/lib/mcp_server/requirements.txt
RUN pip3 install --no-cache-dir -r /usr/local/lib/mcp_server/requirements.txt
COPY mcp_server/mcp_inventory /usr/local/lib/mcp_server/mcp_inventory
COPY scripts/entrypoint.sh /usr/local/bin/entrypoint.sh
EXPOSE 8080
EXPOSE 8081
EXPOSE 3000
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
