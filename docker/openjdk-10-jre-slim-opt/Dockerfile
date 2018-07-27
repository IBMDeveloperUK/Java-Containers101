FROM openjdk:10-jre-slim
EXPOSE 8080
ADD app-cds.jsa /
ADD lib.so /
ADD java-containers101-1.0-SNAPSHOT.jar /
CMD ["java", "-Xshare:off", "-XX:+UseAppCDS", "-XX:SharedArchiveFile=app-cds.jsa", "-XX:AOTLibrary=lib.so", "-jar", "java-containers101-1.0-SNAPSHOT.jar"]
