package tcc.transcricao.tcctranscricaoimage.service.openai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.OpenAIConstants;

@Component
@Slf4j
public class OpenAIPayloadBuilder {

    public String buildImageDescriptionPayload(String base64Image) {
        validateBase64Image(base64Image);

        log.debug("Construindo payload para descrição de imagem");

        String payload = """
        {
           "model": "%s",
           "messages": [
             {
               "role": "system",
               "content": "%s"
             },
             {
               "role": "user",
               "content": [
                 {
                   "type": "image_url",
                   "image_url": {
                     "url": "%s"
                   }
                 }
               ]
             }
           ],
           "max_tokens": %d
         }
        """.formatted(
                OpenAIConstants.GPT_4O_MODEL,
                escapeJsonString(OpenAIConstants.SYSTEM_PROMPT),
                OpenAIConstants.IMAGE_URL_TEMPLATE.formatted(base64Image),
                OpenAIConstants.MAX_TOKENS
        );

        log.debug("Payload construído com sucesso - Tamanho: {} caracteres", payload.length());
        return payload;
    }

    private void validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IllegalArgumentException(OpenAIConstants.INVALID_BASE64_ERROR);
        }

        // Validação básica de formato base64
        if (!base64Image.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            throw new IllegalArgumentException("Formato base64 inválido");
        }
    }

    private String escapeJsonString(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}