package tcc.transcricao.tcctranscricaoimage.service.tts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tcc.transcricao.tcctranscricaoimage.constants.TTSConstants;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;

@Component
@Slf4j
public class TTSHttpClient {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<byte[]> sendTTSRequest(String payload) throws TtsException {
        try {
            log.debug("Enviando requisição TTS para OpenAI");

            HttpHeaders headers = buildHeaders();
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    TTSConstants.OPENAI_TTS_API_URL,
                    HttpMethod.POST,
                    entity,
                    byte[].class
            );

            validateResponse(response);

            log.info("Requisição TTS bem-sucedida - Status: {}, Tamanho: {} bytes",
                    response.getStatusCode(),
                    response.getBody() != null ? response.getBody().length : 0);

            return response;

        } catch (RestClientException e) {
            log.error("Erro na comunicação com OpenAI TTS API", e);
            throw new TtsException(TTSConstants.TTS_API_ERROR_MESSAGE + ": " + e.getMessage(), e);
        }
    }

    private HttpHeaders buildHeaders() throws TtsException {
        validateApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        log.debug("Headers HTTP configurados para TTS");
        return headers;
    }

    private void validateApiKey() throws TtsException {
        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
            throw new TtsException(TTSConstants.API_KEY_ERROR);
        }

        // Validação básica do formato da API key
        if (!openAiApiKey.startsWith("sk-")) {
            log.warn("Formato de API key OpenAI pode estar incorreto");
        }
    }

    private void validateResponse(ResponseEntity<byte[]> response) throws TtsException {
        if (response.getStatusCode() != HttpStatus.OK) {
            String errorMessage = String.format("%s - Status: %s",
                    TTSConstants.TTS_API_ERROR_MESSAGE, response.getStatusCode());

            // Log adicional para debug
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("Erro de autenticação TTS - Verifique a API key");
            } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit excedido na API TTS");
            }

            throw new TtsException(errorMessage);
        }

        if (response.getBody() == null || response.getBody().length == 0) {
            throw new TtsException(TTSConstants.EMPTY_RESPONSE_ERROR);
        }

        // Validação básica do tamanho mínimo esperado para um arquivo de áudio
        if (response.getBody().length < 100) { // Arquivo muito pequeno pode indicar erro
            log.warn("Arquivo de áudio TTS muito pequeno: {} bytes", response.getBody().length);
        }
    }
}