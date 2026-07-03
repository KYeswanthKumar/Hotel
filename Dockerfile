FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN javac -d out -sourcepath src/main/java src/main/java/com/hotelbooking/api/Server.java
EXPOSE 8085
CMD ["java", "-cp", "out", "com.hotelbooking.api.Server"]
