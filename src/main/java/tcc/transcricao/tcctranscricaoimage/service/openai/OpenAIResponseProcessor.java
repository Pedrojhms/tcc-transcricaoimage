package tcc.transcricao.tcctranscricaoimage.service.openai;

import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.OpenAIConstants;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OpenAIResponseProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractDescription(ResponseEntity<byte[]> response) throws DescricaoImagemException {
        try {
            log.debug("Processando resposta da OpenAI");

            byte[] responseBody = response.getBody();
            String contentEncoding = response.getHeaders().getFirst(OpenAIConstants.CONTENT_ENCODING_HEADER);

            JsonNode jsonNode = parseResponseToJson(contentEncoding, responseBody);
            String description = extractDescriptionFromJson(jsonNode);

            log.info("Descrição extraída com sucesso - {} caracteres", description.length());
            return description;

        } catch (IOException e) {
            log.error("Erro ao processar resposta da OpenAI", e);
            throw new DescricaoImagemException(OpenAIConstants.JSON_PARSING_ERROR, e);
        }
    }

    private JsonNode parseResponseToJson(String contentEncoding, byte[] responseBody) throws IOException {
        byte[] jsonBytes = decompressIfNeeded(contentEncoding, responseBody);
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);

        log.debug("JSON da resposta: {} caracteres", jsonString.length());
        return objectMapper.readTree(jsonString);
    }

    private byte[] decompressIfNeeded(String contentEncoding, byte[] responseBody) throws IOException {
        if (OpenAIConstants.BROTLI_ENCODING.equalsIgnoreCase(contentEncoding)) {
            log.debug("Descomprimindo resposta Brotli");
            return decompressBrotli(responseBody);
        } else {
            log.debug("Resposta não comprimida");
            return responseBody;
        }
    }

    private byte[] decompressBrotli(byte[] compressedData) throws IOException {
        try (BrotliInputStream brotliStream = new BrotliInputStream(new ByteArrayInputStream(compressedData))) {
            byte[] decompressed = brotliStream.readAllBytes();
            log.debug("Descompressão Brotli realizada: {} -> {} bytes",
                    compressedData.length, decompressed.length);
            return decompressed;
        } catch (IOException e) {
            log.error("Falha na descompressão Brotli", e);
            throw new IOException(OpenAIConstants.BROTLI_DECOMPRESSION_ERROR, e);
        }
    }

    private String extractDescriptionFromJson(JsonNode jsonNode) throws DescricaoImagemException {
        try {
            JsonNode choicesNode = jsonNode.path(OpenAIConstants.CHOICES_PATH);
            if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.size() == 0) {
                throw new DescricaoImagemException("Estrutura de resposta inválida: 'choices' não encontrado");
            }

            JsonNode firstChoice = choicesNode.get(0);
            JsonNode messageNode = firstChoice.path(OpenAIConstants.MESSAGE_PATH);
            if (messageNode.isMissingNode()) {
                throw new DescricaoImagemException("Estrutura de resposta inválida: 'message' não encontrado");
            }

            JsonNode contentNode = messageNode.path(OpenAIConstants.CONTENT_PATH);
            if (contentNode.isMissingNode()) {
                throw new DescricaoImagemException("Estrutura de resposta inválida: 'content' não encontrado");
            }

            String description = contentNode.asText();
            if (description == null || description.trim().isEmpty()) {
                throw new DescricaoImagemException("Descrição vazia retornada pela OpenAI");
            }

            return description.trim();

        } catch (Exception e) {
            log.error("Erro ao extrair descrição do JSON", e);
            throw new DescricaoImagemException("Erro ao extrair descrição da resposta", e);
        }
    }
}