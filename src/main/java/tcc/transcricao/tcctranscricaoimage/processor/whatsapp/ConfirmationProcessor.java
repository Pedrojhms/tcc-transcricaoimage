package tcc.transcricao.tcctranscricaoimage.processor.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;

@Component
@Slf4j
public class ConfirmationProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String chatId = (String) exchange.getProperty(WhatsAppConstants.PHONE_PROPERTY);

            if (chatId == null || chatId.trim().isEmpty()) {
                throw new IllegalStateException("Número de telefone não encontrado para envio de confirmação");
            }

            JSONObject payload = new JSONObject();
            payload.put("to", chatId);
            payload.put("message", WhatsAppConstants.CONFIRMATION_MESSAGE);

            exchange.getIn().setHeader(WhatsAppConstants.CONTENT_TYPE_HEADER, WhatsAppConstants.APPLICATION_JSON);
            exchange.getIn().setBody(payload.toString());

            log.info("Confirmação preparada para envio ao telefone: {}", chatId);

        } catch (Exception e) {
            log.error("Erro ao preparar confirmação", e);
            throw e;
        }
    }
}