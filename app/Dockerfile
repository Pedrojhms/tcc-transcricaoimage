# Build stage
FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /app

# Copy apenas os arquivos necessários para download de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy código fonte e build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Instalar dependências necessárias
RUN apt-get update && apt-get install -y \
    curl \
    libstdc++6 \
    libc6 \
    && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -g 1001 appgroup && \
    useradd -r -u 1001 -g appgroup appuser

# Criar diretórios necessários
RUN mkdir -p /app/audios /app/logs /app/temp && \
    chown -R appuser:appgroup /app

# Copy do artefato buildado
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

# Configurar volumes
VOLUME ["/app/audios", "/app/logs"]

# Configurar timezone
ENV TZ=America/Sao_Paulo

# Variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Mudar para usuário não-root
USER appuser

# Health check
#HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#    CMD curl -f http://app:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]