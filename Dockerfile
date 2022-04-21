FROM amazoncorretto:17
ADD build/libs/*SNAPSHOT.jar resource-service.jar
ENTRYPOINT ["java","-jar","resource-service.jar"]
