package tcc.transcricao.tcctranscricaoimage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TtsService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] synthesize(String text) {
        String payload = String.format(
                "{" +
                        "\"model\": \"tts-1\"," +
                        "\"input\": \"%s\"," +
                        "\"voice\": \"alloy\"" +
                        "}", text.replace("\"", "\\\"")
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://api.openai.com/v1/audio/speech",
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Erro ao chamar OpenAI TTS: " + response.getStatusCode());
        }

        return response.getBody();
    }
}