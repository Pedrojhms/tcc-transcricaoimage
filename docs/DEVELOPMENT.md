# 👨‍💻 Guia do Desenvolvedor

## 🏗️ Estrutura do Projeto

```
tcc-transcricao-image/
├── src/
│   ├── main/
│   │   ├── java/tcc/transcricao/tcctranscricaoimage/
│   │   │   ├── config/          # Configurações
│   │   │   ├── constants/       # Constantes
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── entity/          # Entidades JPA
│   │   │   ├── exception/       # Exceções customizadas
│   │   │   ├── processor/       # Processor Chains
│   │   │   │   ├── chain/       # Registry e Chains
│   │   │   │   ├── whatsapp/    # Processors WhatsApp
│   │   │   │   ├── survey/      # Processors Survey
│   │   │   │   └── system/      # Processors Sistema
│   │   │   ├── repository/      # Repositórios JPA
│   │   │   ├── route/           # Apache Camel Routes
│   │   │   ├── service/         # Serviços de negócio
│   │   │   └── util/            # Utilitários
│   │   └── resources/
│   │       ├── application.yml   # Configuração principal
│   │       └── logback-spring.xml # Configuração de logs
│   └── test/                     # Testes unitários e integração
├── docs/                         # Documentação
├── docker-compose.yml            # Containers Docker
├── Dockerfile                    # Imagem da aplicação
├── pom.xml                      # Dependências Maven
└── README.md                    # Documentação principal
```

## 🎯 Padrões de Desenvolvimento

### 📋 Convenções de Código

#### Nomenclatura de Classes
```java
// Processors
public class WhatsAppWebhookProcessor implements Processor { }

// Services
@Service
public class ImageAnalysisService { }

// Controllers
@RestController
public class WebhookController { }

// Entities
@Entity
@Table(name = "usuarios")
public class Usuario { }
```

#### Nomenclatura de Métodos
```java
// Processors
public void process(Exchange exchange) throws Exception { }

// Services
public ImageDescription analyzeImage(String imageUrl) { }
public AudioFile generateSpeech(String text) { }

// Repositories
public Optional<Usuario> findByTelefone(String telefone) { }
```

### 🏷️ Anotações Essenciais

```java
// Spring Components
@Component
@Service
@Repository
@Controller
@RestController

// JPA
@Entity
@Table(name = "table_name")
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "column_name")

// Lombok
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j

// Validation
@Valid
@NotNull
@NotEmpty
@Size(min = 1, max = 255)
```

## 🔧 Implementação de Novos Processors

### 📝 Template Base

```java name=src/main/java/tcc/transcricao/tcctranscricaoimage/processor/ExampleProcessor.java
package tcc.transcricao.tcctranscricaoimage.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Processor para [descrever função específica]
 * 
 * Responsabilidades:
 * - [Responsabilidade 1]
 * - [Responsabilidade 2]
 * 
 * @author Pedro Henrique
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExampleProcessor implements Processor {
    
    private final ExampleService exampleService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Iniciando processamento: {}", exchange.getExchangeId());
        
        try {
            // 1. Extrair dados do exchange
            String inputData = exchange.getIn().getBody(String.class);
            String phoneNumber = exchange.getIn().getHeader("phoneNumber", String.class);
            
            // 2. Validar entrada
            validateInput(inputData, phoneNumber);
            
            // 3. Processar dados
            String result = exampleService.processData(inputData);
            
            // 4. Configurar resposta
            exchange.getIn().setBody(result);
            exchange.getIn().setHeader("processedBy", "ExampleProcessor");
            
            log.info("Processamento concluído com sucesso para {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Erro no processamento: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void validateInput(String inputData, String phoneNumber) {
        if (inputData == null || inputData.trim().isEmpty()) {
            throw new IllegalArgumentException("Input data cannot be null or empty");
        }
        
        if (phoneNumber == null || !phoneNumber.matches("\\+?[1-9]\\d{1,14}")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}
```

### 🔗 Adicionando à Chain

```java
// 1. Adicionar ao constructor da chain
@RequiredArgsConstructor
public class ExampleProcessorChain {
    private final ExampleProcessor exampleProcessor;
    private final AnotherProcessor anotherProcessor;
    
    public Processor getExampleProcessor() {
        return exampleProcessor;
    }
}

// 2. Registrar no ProcessorRegistry
@Component
@RequiredArgsConstructor
public class ProcessorRegistry {
    private final ExampleProcessorChain exampleChain;
    
    public ExampleProcessorChain getExampleChain() {
        return exampleChain;
    }
}
```

## 🧪 Testes

### 📋 Estrutura de Testes

```java name=src/test/java/tcc/transcricao/tcctranscricaoimage/processor/ExampleProcessorTest.java
package tcc.transcricao.tcctranscricaoimage.processor;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleProcessorTest {
    
    @Mock
    private ExampleService exampleService;
    
    @InjectMocks
    private ExampleProcessor exampleProcessor;
    
    private Exchange exchange;
    
    @BeforeEach
    void setUp() {
        exchange = new DefaultExchange(new DefaultCamelContext());
    }
    
    @Test
    void testProcessSuccess() throws Exception {
        // Given
        String inputData = "test input";
        String phoneNumber = "+5511999999999";
        String expectedResult = "processed result";
        
        exchange.getIn().setBody(inputData);
        exchange.getIn().setHeader("phoneNumber", phoneNumber);
        
        when(exampleService.processData(inputData)).thenReturn(expectedResult);
        
        // When
        exampleProcessor.process(exchange);
        
        // Then
        assertEquals(expectedResult, exchange.getIn().getBody(String.class));
        assertEquals("ExampleProcessor", exchange.getIn().getHeader("processedBy"));
        verify(exampleService).processData(inputData);
    }
    
    @Test
    void testProcessWithInvalidInput() {
        // Given
        exchange.getIn().setBody(null);
        exchange.getIn().setHeader("phoneNumber", "+5511999999999");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            exampleProcessor.process(exchange);
        });
        
        verifyNoInteractions(exampleService);
    }
}
```

### 🚀 Testes de Integração

```java name=src/test/java/tcc/transcricao/tcctranscricaoimage/integration/WhatsAppIntegrationTest.java
package tcc.transcricao.tcctranscricaoimage.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WhatsAppIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    private final TestRestTemplate restTemplate = new TestRestTemplate();
    
    @Test
    void testWebhookVerification() {
        // Given
        String url = "http://localhost:" + port + "/webhook";
        String params = "?hub.mode=subscribe&hub.verify_token=test&hub.challenge=challenge123";
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url + params, String.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("challenge123", response.getBody());
    }
}
```

## 📊 Banco de Dados

### 🗃️ Criando Nova Entidade

```java name=src/main/java/tcc/transcricao/tcctranscricaoimage/entity/NovaEntidade.java
package tcc.transcricao.tcctranscricaoimage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nova_entidade")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovaEntidade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "campo_obrigatorio", nullable = false)
    private String campoObrigatorio;
    
    @Column(name = "campo_opcional")
    private String campoOpcional;
    
    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
```

### 🔍 Repository Pattern

```java name=src/main/java/tcc/transcricao/tcctranscricaoimage/repository/NovaEntidadeRepository.java
package tcc.transcricao.tcctranscricaoimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tcc.transcricao.tcctranscricaoimage.entity.NovaEntidade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NovaEntidadeRepository extends JpaRepository<NovaEntidade, UUID> {
    
    Optional<NovaEntidade> findByCampoObrigatorio(String campoObrigatorio);
    
    List<NovaEntidade> findByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);
    
    @Query("SELECT n FROM NovaEntidade n WHERE n.campoOpcional IS NOT NULL")
    List<NovaEntidade> findAllWithCampoOpcional();
    
    @Query(value = "SELECT * FROM nova_entidade WHERE campo_obrigatorio ILIKE %:termo%", 
           nativeQuery = true)
    List<NovaEntidade> searchByCampoObrigatorio(@Param("termo") String termo);
}
```

## 🔄 Apache Camel Routes

### 📝 Template de Route

```java name=src/main/java/tcc/transcricao/tcctranscricaoimage/route/ExampleRoute.java
package tcc.transcricao.tcctranscricaoimage.route;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.processor.chain.ProcessorRegistry;

@Component
@RequiredArgsConstructor
public class ExampleRoute extends RouteBuilder {
    
    private final ProcessorRegistry processorRegistry;
    
    @Override
    public void configure() {
        
        // Tratamento de erro global
        onException(Exception.class)
                .handled(true)
                .log("Erro na route Example: ${exception.message}")
                .process(processorRegistry.getSystemChain().getExceptionProcessor());
        
        // Route principal
        from("direct:example-start")
                .routeId("example-route")
                .log("Iniciando Example Route: ${body}")
                .process(processorRegistry.getExampleChain().getExampleProcessor())
                .to("direct:example-next-step")
                .log("Example Route concluída");
        
        // Sub-route
        from("direct:example-next-step")
                .routeId("example-sub-route")
                .log("Processando próximo passo")
                .process(processorRegistry.getExampleChain().getAnotherProcessor())
                .log("Sub-route concluída");
    }
}
```

## 🔧 Configurações

### 📄 Adicionando Nova Configuração

```java name=src/main/java/tcc/transcricao/tcctranscricaoimage/config/ExampleConfig.java
package tcc.transcricao.tcctranscricaoimage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "example")
@Slf4j
public class ExampleConfig {
    
    private String apiUrl;
    private int timeout;
    private boolean enabled;
    
    @Bean
    public ExampleService exampleService() {
        log.info("Configurando ExampleService com URL: {}, timeout: {}s", apiUrl, timeout);
        return new ExampleService(apiUrl, timeout, enabled);
    }
    
    // Getters e Setters
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
```

## 📝 Logs

### 📊 Padrão de Logging

```java
@Slf4j
public class ExampleService {
    
    public String processData(String data) {
        // Log de entrada
        log.info("Iniciando processamento de dados para: {}", 
                 data.length() > 50 ? data.substring(0, 50) + "..." : data);
        
        try {
            // Processamento
            String result = doProcessing(data);
            
            // Log de sucesso
            log.info("Processamento concluído com sucesso. Resultado: {} caracteres", 
                     result.length());
            
            return result;
            
        } catch (Exception e) {
            // Log de erro
            log.error("Erro no processamento de dados: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String doProcessing(String data) {
        // Log de debug
        log.debug("Executando processamento interno com dados: {}", data);
        
        // Lógica de processamento
        return "processed: " + data;
    }
}
```

### 📋 Configuração de Logs

```xml name=src/main/resources/logback-spring.xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Loggers -->
    <logger name="tcc.transcricao" level="INFO" />
    <logger name="org.apache.camel" level="INFO" />
    <logger name="org.springframework" level="WARN" />
    <logger name="org.hibernate" level="WARN" />
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## 🔍 Debug e Troubleshooting

### 🛠️ Debugging Local

```java
// Adicionar breakpoints estratégicos
@Override
public void process(Exchange exchange) throws Exception {
    String body = exchange.getIn().getBody(String.class); // <- Breakpoint aqui
    
    // Debug headers
    exchange.getIn().getHeaders().forEach((key, value) -> {
        log.debug("Header: {} = {}", key, value);
    });
    
    // Lógica de processamento
}
```

### 📊 Métricas Customizadas

```java
@Component
@RequiredArgsConstructor
public class MetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    public void recordProcessingTime(String processType, long timeInMs) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("processing.time")
                .tag("type", processType)
                .register(meterRegistry));
    }
    
    public void incrementCounter(String counterName, String... tags) {
        Counter.builder(counterName)
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }
}
```

## 🚀 Deploy e CI/CD

### 🐳 Dockerfile Otimizado

```dockerfile name=Dockerfile
FROM openjdk:17-jdk-slim

# Instalar dependências
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Configurar diretório de trabalho
WORKDIR /app

# Copiar JAR
COPY target/tcc-transcricao-image-*.jar app.jar

# Configurar propriedades do sistema
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

# Mudar para usuário não-root
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Porta padrão
EXPOSE 8080

# Comando de entrada
ENTRYPOINT ["./entrypoint.sh"]
```

### 🔧 Script de Deploy

```bash name=deploy.sh
#!/bin/bash

echo "🚀 Iniciando deploy do TCC Transcrição Image..."

# Variáveis
APP_NAME="tcc-transcricao-image"
IMAGE_TAG="latest"
CONTAINER_NAME="tcc-app"

# Build da aplicação
echo "📦 Compilando aplicação..."
./mvnw clean package -DskipTests

# Build da imagem Docker
echo "🐳 Construindo imagem Docker..."
docker build -t $APP_NAME:$IMAGE_TAG .

# Parar container existente
echo "🛑 Parando container existente..."
docker stop $CONTAINER_NAME 2>/dev/null || true
docker rm $CONTAINER_NAME 2>/dev/null || true

# Iniciar novo container
echo "▶️ Iniciando novo container..."
docker run -d \
    --name $CONTAINER_NAME \
    --network tcc-network \
    -p 8080:8080 \
    --env-file .env \
    $APP_NAME:$IMAGE_TAG

# Verificar saúde
echo "🔍 Verificando saúde da aplicação..."
sleep 30
curl -f http://localhost:8080/actuator/health && echo "✅ Deploy concluído com sucesso!" || echo "❌ Falha no deploy"
```

## 📚 Recursos Adicionais

### 🔗 Links Úteis

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Apache Camel Documentation](https://camel.apache.org/manual/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [OpenAI API Documentation](https://platform.openai.com/docs/)
- [WhatsApp Business API](https://developers.facebook.com/docs/whatsapp/)

### 🛠️ Ferramentas Recomendadas

- **IDE**: IntelliJ IDEA ou VS Code
- **Database Tool**: DBeaver ou pgAdmin
- **API Testing**: Postman ou Insomnia