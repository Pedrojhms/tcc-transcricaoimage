package tcc.transcricao.tcctranscricaoimage.processor.chain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.processor.whatsapp.ConfirmationProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.whatsapp.VoiceMessageProcessor;
import tcc.transcricao.tcctranscricaoimage.processor.whatsapp.WhatsAppWebhookProcessor;

/**
 * Chain de processors relacionados ao WhatsApp
 * Centraliza operações de webhook, confirmação e mensagens de voz
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WhatsAppProcessorChain {

    private final WhatsAppWebhookProcessor webhookProcessor;
    private final ConfirmationProcessor confirmationProcessor;
    private final VoiceMessageProcessor voiceMessageProcessor;

    /**
     * Processor para webhook do WhatsApp
     * @return WhatsAppWebhookProcessor configurado
     */
    public Processor getWebhookProcessor() {
        log.debug("Acessando WhatsApp Webhook Processor");
        return webhookProcessor;
    }

    /**
     * Processor para confirmação de recebimento
     * @return ConfirmationProcessor configurado
     */
    public Processor getConfirmationProcessor() {
        log.debug("Acessando WhatsApp Confirmation Processor");
        return confirmationProcessor;
    }

    /**
     * Processor para mensagens de voz
     * @return VoiceMessageProcessor configurado
     */
    public Processor getVoiceMessageProcessor() {
        log.debug("Acessando WhatsApp Voice Message Processor");
        return voiceMessageProcessor;
    }

    /**
     * Valida se todos os processors estão disponíveis
     * @return true se todos os processors estão configurados
     */
    public boolean isChainReady() {
        boolean ready = webhookProcessor != null &&
                confirmationProcessor != null &&
                voiceMessageProcessor != null;

        log.debug("WhatsApp Processor Chain ready: {}", ready);
        return ready;
    }
}