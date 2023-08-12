FROM amazoncorretto:17-alpine-jdk
MAINTAINER nefariusmag@gmail.com
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]