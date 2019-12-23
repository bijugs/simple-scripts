FROM openjdk:7
COPY flink-data-generator-1.0-SNAPSHOT.jar /usr/lib/
COPY wordlist.txt /tmp/
CMD ["java", "-cp", "/usr/lib/flink-data-generator-1.0-SNAPSHOT.jar", "com.ssamples.flink.datagen.WordGenServer", "/tmp/wordlist.txt", "9000"]
