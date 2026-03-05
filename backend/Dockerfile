FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the WAR file
RUN mvn -q -DskipTests package

# Runtime image with Tomcat
FROM tomcat:9.0-jdk11

RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR from the build stage into Tomcat as ROOT.war
COPY --from=build /app/target/smart-queue.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
