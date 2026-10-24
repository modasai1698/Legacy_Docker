# ---- Stage 1: build the WAR with Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY server/pom.xml server/pom.xml
COPY webapp/pom.xml webapp/pom.xml
RUN mvn -q -B dependency:go-offline || true
COPY . .
RUN mvn -B -q clean package

# ---- Stage 2: run on Tomcat 10.1 (Jakarta EE 10) + JRE 21 ----
FROM tomcat:10.1-jre21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/webapp/target/webapp.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
