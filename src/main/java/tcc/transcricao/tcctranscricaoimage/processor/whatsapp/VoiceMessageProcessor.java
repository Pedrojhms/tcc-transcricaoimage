package tcc.transcricao.tcctranscricaoimage.processor.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;

@Component
@Slf4j
public class VoiceMessageProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String to = (String) exchange.getProperty(WhatsAppConstants.PHONE_PROPERTY);
            String audioBase64 = (String) exchange.getProperty(WhatsAppConstants.AUDIO_BASE64_PROPERTY);

            // Validações
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalStateException("Número de telefone não encontrado para envio de voz");
            }
            if (audioBase64 == null || audioBase64.trim().isEmpty()) {
                throw new IllegalStateException("Áudio base64 não encontrado para envio");
            }

            JSONObject payload = new JSONObject();
            payload.put("to", to);
            payload.put("audioBase64", audioBase64);

            exchange.getIn().setHeader(WhatsAppConstants.CONTENT_TYPE_HEADER, WhatsAppConstants.APPLICATION_JSON);
            exchange.getIn().setBody(payload.toString());

            log.info("Mensagem de voz preparada para envio ao telefone: {}", to);

        } catch (Exception e) {
            log.error("Erro ao preparar mensagem de voz", e);
            throw e;
        }
    }
}