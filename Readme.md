# TCC - Transcrição de imagem

Projeto para transcrição de imagens com integração ao WhatsApp, utilizando Java (Spring Boot) e Node.js.

## Descrição

O sistema recebe imagens via WhatsApp, utiliza a API da OpenAI para descrever essas imagens e converte a descrição em áudio, enviando como mensagem de voz para o usuário. Métricas de desempenho são salvas no banco de dados para análise posterior.

## Principais Funcionalidades

- **Recebimento de imagens via WhatsApp**: Utiliza `whatsapp-web.js` para receber imagens enviadas por usuários.
- **Descrição de imagens usando OpenAI**: O backend em Java consome a API do OpenAI para gerar uma descrição textual das imagens recebidas.
- **Conversão de texto em áudio**: Utiliza a API de TTS (Text-to-Speech) da OpenAI para transformar a descrição em áudio.
- **Envio de áudio via WhatsApp**: O áudio é enviado para o usuário como mensagem de voz.
- **Armazenamento de métricas**: Métricas de performance (tempos de processamento) são salvas no banco de dados PostgreSQL.
- **Logs detalhados**: Tempos de cada etapa são logados para análise.

## Estrutura de Pastas

- `src/main/java/tcc/transcricao/tcctranscricaoimage/`: Backend em Java (Spring Boot)
    - `service/`: Serviços de integração com OpenAI (descrição, TTS, armazenamento de áudio)
    - `route/`: Rotas Camel para processamento dos fluxos
    - `repository/`: Repositórios JPA para acesso ao banco
    - `model/`: Modelos de dados (ex: PerformanceMetric)
- `wpp/`: Aplicação Node.js para integração com WhatsApp
    - `index.js`: Principal lógica de recebimento/envio de mensagens
    - `Dockerfile`: Containerização do serviço WhatsApp

## Principais arquivos

- **Java**
    - `TccTranscricaoimageApplication.java`: Main da aplicação Spring Boot
    - `AudioStorageService.java`: Serviço para salvar arquivos de áudio
    - `ImageDescriptionService.java`: Serviço para descrever imagens usando OpenAI
    - `TtsService.java`: Serviço para converter texto em áudio usando OpenAI
    - `DescribeImageRoute.java`: Rotas Camel que orquestram o processamento de imagem/áudio e envio das respostas
    - `PerformanceMetricRepository.java`: Repositório JPA para métricas
- **Node.js**
    - `index.js`: Recebe mensagens, processa mídia, envia para o backend e retorna respostas via WhatsApp
    - `Dockerfile`: Permite rodar o serviço WhatsApp em container

## Como funciona o fluxo

1. Usuário envia uma imagem via WhatsApp.
2. O serviço Node.js (`wpp/index.js`) recebe a imagem, extrai o base64 e envia para o backend via webhook.
3. O backend processa:
    - Gera descrição textual usando OpenAI.
    - Converte a descrição em áudio.
    - Salva métricas de desempenho.
    - Retorna o áudio para o serviço Node.js, que envia para o usuário.
4. Todo processamento é logado e as métricas são salvas em `performance_metric` no banco PostgreSQL.

## Requisitos

- Java 21+
- Node.js 20+
- Docker (para rodar o WhatsApp gateway)
- PostgreSQL

## Configuração

- Variáveis de ambiente para backend:
    - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (conexão com PostgreSQL)
    - `OPENAI_API_KEY` (chave da API OpenAI)

### Configuração das variáveis de ambiente (Linux)

Inclua as linhas abaixo no final do seu arquivo `~/.bashrc` para que as variáveis de ambiente estejam sempre disponíveis:

```bash
nano ~/.bashrc

export DB_USERNAME="tccuser"
export DB_PASSWORD="senha_forte"
export DB_URL="jdbc:postgresql://db:5432/tcc_db"
export OPENAI_API_KEY="sua_api_key_aqui"

source ~/.bashrc

docker-compose down
docker-compose up -d
docker exec -it nome_do_container bash

echo $DB_USERNAME
echo $DB_PASSWORD
echo $DB_URL
echo $OPENAI_API_KEY
```

## Licença

Projeto acadêmico - sem licença definida.
