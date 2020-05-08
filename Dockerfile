FROM openjdk:8

WORKDIR /app/
COPY ladeit.jar /app/
ENTRYPOINT ["java","-jar","/app/ladeit.jar"]