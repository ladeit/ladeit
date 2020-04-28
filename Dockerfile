FROM openjdk:8

WORKDIR /app/
COPY ladeit.jar /app/
ENTRYPOINT ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=7899","/app/ladeit.jar"]