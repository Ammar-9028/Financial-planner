# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package

# ---- run: Tomcat 9 on Java 21 ----
FROM tomcat:9.0-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/* \
 && groupadd -r app && useradd -r -g app app
COPY --from=build /src/target/finplan.war /usr/local/tomcat/webapps/ROOT.war
RUN chown -R app:app /usr/local/tomcat
USER app
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/urandom"
EXPOSE 8080
CMD ["catalina.sh", "run"]
