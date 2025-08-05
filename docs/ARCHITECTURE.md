# 🏗️ Arquitetura do Sistema

## 📐 Visão Geral

O sistema de transcrição de imagens é construído usando uma arquitetura baseada em **Enterprise Integration Patterns** com Apache Camel, implementando o padrão **Processor Chains** para melhor organização e manutenibilidade.

## 🎯 Arquitetura de Alto Nível

```mermaid
graph TB
    subgraph "Camada de Apresentação"
        A[WhatsApp Business API]
    end
    
    subgraph "Camada de Integração"
        B[Apache Camel Routes]
        C[Processor Chains]
    end
    
    subgraph "Camada de Negócio"
        D[WhatsApp Processors]
        E[Survey Processors]
        F[System Processors]
    end
    
    subgraph "Camada de Serviços Externos"
        G[OpenAI API]
        H[WhatsApp Graph API]
    end
    
    subgraph "Camada de Dados"
        I[PostgreSQL Database]
    end
    
    A --> B
    B --> C
    C --> D
    C --> E
    C --> F
    D --> G
    D --> H
    E --> I
    F --> I
```

## 🔗 Processor Chains

### 📱 WhatsAppProcessorChain

Responsável por todas as operações relacionadas ao WhatsApp:

```mermaid
graph LR
    A[WhatsApp Webhook] --> B[WhatsAppWebhookProcessor]
    B --> C[ConfirmationProcessor]
    B --> D[VoiceMessageProcessor]
```

**Componentes:**
- `WhatsAppWebhookProcessor`: Processa webhooks recebidos
- `ConfirmationProcessor`: Envia confirmações de recebimento
- `VoiceMessageProcessor`: Processa e envia mensagens de voz

### 📊 SurveyProcessorChain

Gerencia o sistema de pesquisa de satisfação:

```mermaid
graph LR
    A[Survey Start] --> B[SurveyPreparationProcessor]
    B --> C[SurveyProcessor]
    C --> D[SurveyMessageProcessor]
    D --> E[SurveyResponseProcessor]
```

**Componentes:**
- `SurveyPreparationProcessor`: Prepara dados da pesquisa
- `SurveyProcessor`: Lógica principal da pesquisa
- `SurveyMessageProcessor`: Formata mensagens da pesquisa
- `SurveyResponseProcessor`: Processa respostas dos usuários

### ⚙️ SystemProcessorChain

Operações de sistema e infraestrutura:

```mermaid
graph LR
    A[System Operations] --> B[ImageAudioProcessor]
    A --> C[PerformanceMetricsProcessor]
    A --> D[ExceptionToHttpResponseProcessor]
```

**Componentes:**
- `ImageAudioProcessor`: Processa imagens e gera áudio
- `PerformanceMetricsProcessor`: Coleta métricas de performance
- `ExceptionToHttpResponseProcessor`: Trata exceções e respostas HTTP

## 🔄 Fluxo de Dados Principal

```mermaid
sequenceDiagram
    participant U as Usuário
    participant W as WhatsApp API
    participant WP as Webhook Processor
    participant IAP as Image Audio Processor
    participant AI as OpenAI API
    participant DB as PostgreSQL
    
    U->>W: Envia Imagem
    W->>WP: Webhook Event
    WP->>W: Confirmação
    WP->>IAP: Processa Imagem
    IAP->>AI: Analisa Imagem (GPT-4o)
    AI->>IAP: Descrição da Imagem
    IAP->>AI: Converte para Áudio (TTS)
    AI->>IAP: Arquivo de Áudio
    IAP->>W: Envia Áudio
    W->>U: Recebe Áudio
    IAP->>DB: Salva Métricas
```

## 🏛️ Padrões Arquiteturais

### 📋 Registry Pattern

```java
@Component
public class ProcessorRegistry {
    private final WhatsAppProcessorChain whatsAppChain;
    private final SurveyProcessorChain surveyChain;
    private final SystemProcessorChain systemChain;
    
    // Acesso centralizado a todas as chains
}
```

### 🔗 Chain of Responsibility

Cada chain agrupa processors relacionados, facilitando:
- **Manutenibilidade**: Alterações isoladas por domínio
- **Testabilidade**: Testes unitários focados
- **Extensibilidade**: Fácil adição de novos processors

### ⚡ Enterprise Integration Patterns

**Implementados com Apache Camel:**
- **Message Router**: Direcionamento de mensagens
- **Content Enricher**: Enriquecimento de dados
- **Wire Tap**: Processamento paralelo
- **Dead Letter Channel**: Tratamento de erros

## 📊 Modelo de Dados

```mermaid
erDiagram
    USUARIO {
        string telefone PK
        string nome
        timestamp criado_em
        timestamp atualizado_em
    }
    
    INTERACAO {
        uuid id PK
        string telefone FK
        string tipo_interacao
        text conteudo
        timestamp processado_em
        float tempo_processamento
    }
    
    PESQUISA_SATISFACAO {
        uuid id PK
        string telefone FK
        int nota_satisfacao
        text comentario
        timestamp respondido_em
    }
    
    METRICAS_PERFORMANCE {
        uuid id PK
        string endpoint
        float tempo_resposta
        string status
        timestamp timestamp
    }
    
    USUARIO ||--o{ INTERACAO : tem
    USUARIO ||--o{ PESQUISA_SATISFACAO : responde
```

## 🔧 Configuração de Ambiente

### Variáveis Críticas

```properties
# OpenAI Configuration
openai.api.key=${OPENAI_API_KEY}
openai.model.vision=gpt-4o
openai.model.tts=tts-1

# WhatsApp Business API
whatsapp.api.token=${WHATSAPP_TOKEN}
whatsapp.phone.number.id=${WHATSAPP_PHONE_ID}
whatsapp.webhook.verify.token=${WEBHOOK_VERIFY_TOKEN}

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## 📈 Métricas e Monitoramento

### KPIs Principais

- **Tempo de Resposta**: Média de processamento por imagem
- **Taxa de Sucesso**: Percentual de processamentos bem-sucedidos
- **Satisfação do Usuário**: Média das notas da pesquisa
- **Volume de Processamento**: Número de imagens processadas por período

### Logging Structure

```json
{
  "timestamp": "2024-01-10T12:00:00Z",
  "level": "INFO",
  "service": "image-transcription",
  "processor": "ImageAudioProcessor",
  "phone": "+5511999999999",
  "processing_time": 2.3,
  "status": "SUCCESS"
}
```

## 🚀 Escalabilidade

### Estratégias Implementadas

1. **Processamento Assíncrono**: Apache Camel routes não-bloqueantes
2. **Pool de Conexões**: Configuração otimizada do banco
3. **Cache de Resultados**: Cache em memória para operações frequentes
4. **Rate Limiting**: Controle de taxa para APIs externas

### Pontos de Melhoria

- **Containerização**: Docker para deployment
- **Load Balancing**: Múltiplas instâncias
- **Cache Distribuído**: Redis para cache compartilhado
- **Message Queue**: RabbitMQ para alta disponibilidade