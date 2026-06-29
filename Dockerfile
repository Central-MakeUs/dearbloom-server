FROM eclipse-temurin:21-jdk

COPY build/libs/dearbloom-server-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
