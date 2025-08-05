package tcc.transcricao.tcctranscricaoimage.processor.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.WhatsAppConstants;
import tcc.transcricao.tcctranscricaoimage.service.ImageDescriptionService;
import tcc.transcricao.tcctranscricaoimage.service.TtsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageAudioProcessor implements Processor {

    private final ImageDescriptionService imageDescriptionService;
    private final TtsService ttsService;

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            // Etapa 1: Gerar descrição da imagem
            processImageDescription(exchange);

            // Etapa 2: Sintetizar áudio
            processAudioSynthesis(exchange);

        } catch (Exception e) {
            log.error("Erro durante processamento de imagem e áudio", e);
            throw e;
        }
    }

    private void processImageDescription(Exchange exchange) throws Exception {
        String imageBase64 = (String) exchange.getProperty(WhatsAppConstants.IMAGE_BASE64_PROPERTY);

        if (imageBase64 == null || imageBase64.trim().isEmpty()) {
            throw new IllegalStateException("Imagem base64 não encontrada para processamento");
        }

        log.debug("Iniciando descrição da imagem");
        String descricao = imageDescriptionService.getDescription(imageBase64);

        exchange.setProperty(WhatsAppConstants.DESCRIPTION_PROPERTY, descricao);
        exchange.setProperty(WhatsAppConstants.DESC_TIME_PROPERTY, System.currentTimeMillis());

        log.info("Descrição gerada com sucesso: {} caracteres", descricao.length());
    }

    private void processAudioSynthesis(Exchange exchange) throws Exception {
        String descricao = (String) exchange.getProperty(WhatsAppConstants.DESCRIPTION_PROPERTY);

        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalStateException("Descrição não encontrada para síntese de áudio");
        }

        log.debug("Iniciando síntese de áudio");
        String audioBase64 = ttsService.synthesizeAsBase64(descricao);

        exchange.setProperty(WhatsAppConstants.AUDIO_BASE64_PROPERTY, audioBase64);
        exchange.setProperty(WhatsAppConstants.TTS_TIME_PROPERTY, System.currentTimeMillis());

        log.info("Áudio sintetizado com sucesso: {} caracteres base64", audioBase64.length());
    }
}