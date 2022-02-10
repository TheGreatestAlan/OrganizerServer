#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY . /home/app/src
RUN mvn -f /home/app/src/pom.xml clean package 

#
# Package stage
#
FROM amazoncorretto:11
COPY --from=build /home/app/src/target/organizerserver-1.0-SNAPSHOT.jar /usr/local/lib/organizer.jar
COPY --from=build /home/app/src/dev/configuration.yml /usr/local/lib/configuration.yml
#EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/organizer.jar", "server", "/usr/local/lib/configuration.yml"]
