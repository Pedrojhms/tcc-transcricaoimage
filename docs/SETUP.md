# 🛠️ Guia de Configuração e Instalação

## 📋 Pré-requisitos

### 🖥️ Sistema Operacional
- Linux (Ubuntu 20.04+ recomendado)
- macOS (Big Sur+)
- Windows 10+ (com WSL2)

### ⚙️ Software Necessário

| Software | Versão Mínima | Download |
|----------|---------------|----------|
| Java JDK | 21+           | [OpenJDK](https://openjdk.java.net/) |
| Maven | 3.8+          | [Apache Maven](https://maven.apache.org/) |
| Docker | 20.10+        | [Docker](https://www.docker.com/) |
| Docker Compose | 2.0+          | [Docker Compose](https://docs.docker.com/compose/) |
| PostgreSQL | 15+           | [PostgreSQL](https://www.postgresql.org/) |

## 🔑 Configuração de APIs

### 🤖 OpenAI API

1. **Criar conta na OpenAI:**
    - Acesse [platform.openai.com](https://platform.openai.com/)
    - Crie uma conta ou faça login

2. **Gerar API Key:**
   ```bash
   # Navegue para API Keys
   # Clique em "Create new secret key"
   # Copie e guarde a chave com segurança
   ```

3. **Configurar créditos:**
    - Adicione método de pagamento
    - Configure limites de uso

### 📱 WhatsApp Business API

1. **Meta for Developers:**
    - Acesse [developers.facebook.com](https://developers.facebook.com/)
    - Crie um app "WhatsApp Business"

2. **Configurar WhatsApp:**
   ```bash
   # Obter Phone Number ID
   # Gerar Access Token permanente
   # Configurar webhook URL
   ```

3. **Verificar número:**
    - Adicione número de teste
    - Verifique via SMS/chamada

## 🗃️ Configuração do Banco de Dados

### 🐘 PostgreSQL com Docker

```bash
# 1. Criar container PostgreSQL
docker run --name tcc-postgres \
  -e POSTGRES_DB=tcc_transcricao \
  -e POSTGRES_USER=tcc_user \
  -e POSTGRES_PASSWORD=tcc_password \
  -p 5432:5432 \
  -d postgres:15

# 2. Verificar se está rodando
docker ps

# 3. Conectar ao banco (opcional)
docker exec -it tcc-postgres psql -U tcc_user -d tcc_transcricao
```

### 📊 Estrutura do Banco

```sql
-- Criação das tabelas principais
CREATE TABLE usuarios (
    telefone VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(255),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interacoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telefone VARCHAR(20) REFERENCES usuarios(telefone),
    tipo_interacao VARCHAR(50) NOT NULL,
    conteudo TEXT,
    processado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tempo_processamento FLOAT
);

CREATE TABLE pesquisa_satisfacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telefone VARCHAR(20) REFERENCES usuarios(telefone),
    nota_satisfacao INTEGER CHECK (nota_satisfacao BETWEEN 1 AND 5),
    comentario TEXT,
    respondido_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE metricas_performance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    endpoint VARCHAR(255),
    tempo_resposta FLOAT,
    status VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔧 Configuração da Aplicação

### 📁 Variáveis de Ambiente

Crie o arquivo `.env` na raiz do projeto:

```bash name=.env
# ===== DATABASE CONFIGURATION =====
DATABASE_URL=jdbc:postgresql://localhost:5432/tcc_transcricao
DB_USERNAME=tcc_user
DB_PASSWORD=tcc_password

# ===== OPENAI CONFIGURATION =====
OPENAI_API_KEY=sk-your-openai-api-key-here
OPENAI_MODEL_VISION=gpt-4o
OPENAI_MODEL_TTS=tts-1
OPENAI_TTS_VOICE=alloy

# ===== WHATSAPP BUSINESS API =====
WHATSAPP_TOKEN=your-whatsapp-access-token
WHATSAPP_PHONE_ID=your-phone-number-id
WHATSAPP_BUSINESS_ACCOUNT_ID=your-business-account-id
WEBHOOK_VERIFY_TOKEN=your-webhook-verify-token

# ===== APPLICATION CONFIGURATION =====
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
LOG_LEVEL=INFO

# ===== WEBHOOK CONFIGURATION =====
WEBHOOK_BASE_URL=https://your-domain.com
WEBHOOK_ENDPOINT=/webhook

# ===== PERFORMANCE TUNING =====
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### 📄 application.yml

```yaml name=src/main/resources/application.yml
spring:
  application:
    name: tcc-transcricao-image
  
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        
  hikari:
    maximum-pool-size: ${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:10}
    minimum-idle: ${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:5}

# OpenAI Configuration
openai:
  api:
    key: ${OPENAI_API_KEY}
  model:
    vision: ${OPENAI_MODEL_VISION:gpt-4o}
    tts: ${OPENAI_MODEL_TTS:tts-1}
  tts:
    voice: ${OPENAI_TTS_VOICE:alloy}

# WhatsApp Business API
whatsapp:
  api:
    token: ${WHATSAPP_TOKEN}
    base-url: https://graph.facebook.com/v19.0
  phone:
    number:
      id: ${WHATSAPP_PHONE_ID}
  business:
    account:
      id: ${WHATSAPP_BUSINESS_ACCOUNT_ID}
  webhook:
    verify:
      token: ${WEBHOOK_VERIFY_TOKEN}

# Server Configuration
server:
  port: ${SERVER_PORT:8080}

# Logging
logging:
  level:
    tcc.transcricao: ${LOG_LEVEL:INFO}
    org.apache.camel: INFO
    org.springframework: WARN
    org.hibernate: WARN
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    console: "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: always
```

## 🐳 Docker Compose

```yaml name=docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: tcc-postgres
    environment:
      POSTGRES_DB: tcc_transcricao
      POSTGRES_USER: tcc_user
      POSTGRES_PASSWORD: tcc_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - tcc-network

  redis:
    image: redis:7-alpine
    container_name: tcc-redis
    ports:
      - "6379:6379"
    networks:
      - tcc-network

  app:
    build: .
    container_name: tcc-app
    depends_on:
      - postgres
      - redis
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/tcc_transcricao
      DB_USERNAME: tcc_user
      DB_PASSWORD: tcc_password
      SPRING_PROFILES_ACTIVE: docker
    ports:
      - "8080:8080"
    networks:
      - tcc-network
    env_file:
      - .env

volumes:
  postgres_data:

networks:
  tcc-network:
    driver: bridge
```

## 🚀 Instalação Passo a Passo

### 1️⃣ Clone do Repositório

```bash
# Clone o projeto
git clone https://github.com/Pedrojhms/tcc-transcricao-image.git
cd tcc-transcricao-image

# Verifique a estrutura
ls -la
```

### 2️⃣ Configuração de Ambiente

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite com suas credenciais
nano .env
# ou
code .env
```

### 3️⃣ Inicialização dos Serviços

```bash
# Inicie o PostgreSQL
docker-compose up -d postgres

# Aguarde o banco estar pronto
docker-compose logs postgres

# Execute as migrações (se necessário)
./mvnw flyway:migrate

# Inicie todos os serviços
docker-compose up -d
```

### 4️⃣ Compilação e Execução

```bash
# Compilar o projeto
./mvnw clean compile

# Executar testes
./mvnw test

# Executar a aplicação
./mvnw spring-boot:run
```

### 5️⃣ Verificação da Instalação

```bash
# Health check
curl http://localhost:8080/actuator/health

# Teste do webhook
curl -X GET "http://localhost:8080/webhook?hub.mode=subscribe&hub.verify_token=your-token&hub.challenge=test"
```

## 🔧 Configuração do Webhook

### 🌐 Ngrok (Desenvolvimento)

```bash
# Instalar ngrok
npm install -g ngrok

# Expor porta local
ngrok http 8080

# Usar URL fornecida no WhatsApp
# Exemplo: https://abc123.ngrok.io/webhook
```

### ☁️ Deploy em Produção

```bash
# Configurar domínio
WEBHOOK_URL=https://seu-dominio.com/webhook

# Configurar no WhatsApp
curl -X POST \
  "https://graph.facebook.com/v19.0/${WHATSAPP_PHONE_ID}/webhooks" \
  -H "Authorization: Bearer ${WHATSAPP_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "webhook_url": "'${WEBHOOK_URL}'",
    "verify_token": "'${WEBHOOK_VERIFY_TOKEN}'"
  }'
```

## ⚠️ Troubleshooting

### 🚨 Problemas Comuns

#### Erro de Conexão com Banco
```bash
# Verificar se o PostgreSQL está rodando
docker ps | grep postgres

# Verificar logs
docker logs tcc-postgres

# Resetar container
docker-compose down
docker-compose up -d postgres
```

#### Erro de API Key OpenAI
```bash
# Verificar se a chave está válida
curl -H "Authorization: Bearer ${OPENAI_API_KEY}" \
  https://api.openai.com/v1/models

# Verificar créditos disponíveis
curl -H "Authorization: Bearer ${OPENAI_API_KEY}" \
  https://api.openai.com/v1/usage
```

#### Webhook não funciona
```bash
# Verificar se o webhook está acessível
curl -X GET "http://localhost:8080/webhook?hub.mode=subscribe&hub.verify_token=test&hub.challenge=challenge"

# Verificar logs da aplicação
tail -f logs/application.log

# Testar com ngrok
ngrok http 8080
```

### 📊 Monitoramento

```bash
# Verificar métricas
curl http://localhost:8080/actuator/metrics

# Verificar logs
tail -f logs/application.log

# Monitorar banco de dados
docker exec -it tcc-postgres psql -U tcc_user -d tcc_transcricao -c "SELECT * FROM metricas_performance ORDER BY timestamp DESC LIMIT 10;"
```

## 🎯 Próximos Passos

Após a instalação bem-sucedida:

1. 📱 Configure o número do WhatsApp de teste
2. 🖼️ Envie uma imagem para testar
3. 📊 Verifique as métricas no banco
4. 🔍 Monitore os logs da aplicação
5. 📈 Configure alertas de monitoramento

---

**✅ Instalação concluída com sucesso!**  
O sistema está pronto para receber e processar imagens via WhatsApp.