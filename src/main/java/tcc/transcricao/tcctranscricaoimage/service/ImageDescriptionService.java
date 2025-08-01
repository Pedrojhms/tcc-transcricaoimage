package tcc.transcricao.tcctranscricaoimage.service;

import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ImageDescriptionService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getDescription(String base64Image) throws Exception {
        String payload = buildPayload(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                byte[].class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Erro ao chamar OpenAI: " + response.getStatusCode());
        }

        byte[] compressedOrJson = response.getBody();
        String contentEncoding = response.getHeaders().getFirst("Content-Encoding");
        JsonNode root = getJsonNode(contentEncoding, compressedOrJson);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private static JsonNode getJsonNode(String contentEncoding, byte[] compressedOrJson) throws IOException {
        byte[] jsonBytes;
        if ("br".equalsIgnoreCase(contentEncoding)) {
            assert compressedOrJson != null;
            try (BrotliInputStream brotli = new BrotliInputStream(new ByteArrayInputStream(compressedOrJson))) {
                jsonBytes = brotli.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("Erro ao descompactar resposta Brotli", e);
            }
        } else {
            jsonBytes = compressedOrJson;
        }

        assert jsonBytes != null;
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    private String buildPayload(String base64Image) {
        return """
        {
           "model": "gpt-4o",
           "messages": [
             {
               "role": "system",
               "content": "Descreva a imagem de forma clara, objetiva e sensorial, com até 100 palavras, como se estivesse guiando uma pessoa com deficiência visual. Destaque os elementos principais da cena, o contexto, as cores predominantes e detalhes visuais marcantes. Se houver pessoas na imagem, não tente identificar quem são. Em vez disso, descreva características visuais como expressões faciais, postura, cor e estilo das roupas, penteado, tom de pele e outros traços visíveis, ressaltando os pontos fortes de sua aparência de maneira respeitosa e inclusiva. Evite termos vagos como “bonito” ou “agradável” e priorize uma descrição útil, empática e descritiva."
             },
             {
               "role": "user",
               "content": [
                 {
                   "type": "image_url",
                   "image_url": {
                     "url": "data:image/jpeg;base64,%s"
                   }
                 }
               ]
             }
           ],
           "max_tokens": 200
         }
        """.formatted(base64Image);
    }
}