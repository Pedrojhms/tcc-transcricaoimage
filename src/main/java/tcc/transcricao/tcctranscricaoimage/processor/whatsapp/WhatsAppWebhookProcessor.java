package tcc.transcricao.tcctranscricaoimage.processor.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;

import java.util.UUID;

@Component
@Slf4j
public class WhatsAppWebhookProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String body = exchange.getIn().getBody(String.class);
            log.debug("Processando webhook: {}", body);

            JSONObject webhookJson = new JSONObject(body);

            // Validação de campos obrigatórios
            validateWebhookData(webhookJson);

            String phone = webhookJson.getString("from");
            String imageBase64 = webhookJson.getJSONObject("media").getString("data");
            String imageId = UUID.randomUUID().toString();

            // Configuração das propriedades do exchange
            exchange.setProperty(WhatsAppConstants.START_TIME_PROPERTY, System.currentTimeMillis());
            exchange.setProperty(WhatsAppConstants.IMAGE_ID_PROPERTY, imageId);
            exchange.setProperty(WhatsAppConstants.PHONE_PROPERTY, phone);
            exchange.setProperty(WhatsAppConstants.IMAGE_BASE64_PROPERTY, imageBase64);

            log.info("Webhook processado - Telefone: {}, ImageId: {}", phone, imageId);

        } catch (Exception e) {
            log.error("Erro ao processar webhook do WhatsApp", e);
            throw e;
        }
    }

    private void validateWebhookData(JSONObject webhookJson) {
        if (!webhookJson.has("from")) {
            throw new IllegalArgumentException("Campo 'from' é obrigatório no webhook");
        }
        if (!webhookJson.has("media") || !webhookJson.getJSONObject("media").has("data")) {
            throw new IllegalArgumentException("Campo 'media.data' é obrigatório no webhook");
        }
    }
}