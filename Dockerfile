FROM openjdk:11
WORKDIR /
ADD target/databus-moss-1.0-SNAPSHOT.jar app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 8080
CMD java -jar -Dspring.profiles.active=prod app.jar
