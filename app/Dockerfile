FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/audios \
    && apt-get update \
    && apt-get install -y libstdc++6 \
    && rm -rf /var/lib/apt/lists/*
VOLUME /app/audios
EXPOSE 8080
ENV OPENAI_API_KEY=""
ENTRYPOINT ["java", "-jar", "app.jar"]