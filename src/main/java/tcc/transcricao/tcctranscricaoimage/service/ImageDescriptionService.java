package tcc.transcricao.tcctranscricaoimage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.service.openai.OpenAIHttpClient;
import tcc.transcricao.tcctranscricaoimage.service.openai.OpenAIPayloadBuilder;
import tcc.transcricao.tcctranscricaoimage.service.openai.OpenAIResponseProcessor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageDescriptionService {

    private final OpenAIPayloadBuilder payloadBuilder;
    private final OpenAIHttpClient httpClient;
    private final OpenAIResponseProcessor responseProcessor;

    public String getDescription(String base64Image) throws DescricaoImagemException {
        log.info("Iniciando processo de descrição de imagem");

        try {
            // Etapa 1: Construir payload
            String payload = payloadBuilder.buildImageDescriptionPayload(base64Image);
            log.debug("Payload construído com sucesso");

            // Etapa 2: Enviar requisição
            ResponseEntity<byte[]> response = httpClient.sendRequest(payload);
            log.debug("Requisição enviada com sucesso");

            // Etapa 3: Processar resposta
            String description = responseProcessor.extractDescription(response);
            log.info("Descrição obtida com sucesso");

            return description;

        } catch (DescricaoImagemException e) {
            log.error("Erro específico na descrição de imagem: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado na descrição de imagem", e);
            throw new DescricaoImagemException("Erro inesperado ao processar descrição da imagem", e);
        }
    }
}