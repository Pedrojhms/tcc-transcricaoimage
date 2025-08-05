package tcc.transcricao.tcctranscricaoimage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tcc.transcricao.tcctranscricaoimage.constants.TTSConstants;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;
import tcc.transcricao.tcctranscricaoimage.service.tts.TTSHttpClient;
import tcc.transcricao.tcctranscricaoimage.service.tts.TTSPayloadBuilder;
import tcc.transcricao.tcctranscricaoimage.service.tts.TTSResponseProcessor;

@Service
@RequiredArgsConstructor
@Slf4j
public class TtsService {

    private final TTSPayloadBuilder payloadBuilder;
    private final TTSHttpClient httpClient;
    private final TTSResponseProcessor responseProcessor;

    public byte[] synthesize(String text) throws TtsException {
        log.info("Iniciando síntese de voz - {} caracteres", text != null ? text.length() : 0);

        try {
            // Etapa 1: Construir payload
            String payload = payloadBuilder.buildSpeechPayload(text);
            log.debug(TTSConstants.PAYLOAD_BUILT_LOG);

            // Etapa 2: Enviar requisição
            ResponseEntity<byte[]> response = httpClient.sendTTSRequest(payload);
            log.debug(TTSConstants.REQUEST_SENT_LOG);

            // Etapa 3: Processar resposta
            byte[] audioBytes = responseProcessor.extractAudioBytes(response);
            log.debug(TTSConstants.RESPONSE_PROCESSED_LOG);

            log.info(TTSConstants.TTS_SUCCESS_LOG + " - {} bytes de áudio", audioBytes.length);
            return audioBytes;

        } catch (TtsException e) {
            log.error("Erro específico na síntese TTS: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado na síntese TTS", e);
            throw new TtsException("Erro inesperado durante síntese de voz", e);
        }
    }

    public String synthesizeAsBase64(String text) throws TtsException {
        log.info("Iniciando síntese de voz para Base64 - {} caracteres", text != null ? text.length() : 0);

        try {
            // Etapa 1: Construir payload
            String payload = payloadBuilder.buildSpeechPayload(text);
            log.debug(TTSConstants.PAYLOAD_BUILT_LOG);

            // Etapa 2: Enviar requisição
            ResponseEntity<byte[]> response = httpClient.sendTTSRequest(payload);
            log.debug(TTSConstants.REQUEST_SENT_LOG);

            // Etapa 3: Processar resposta e converter para Base64
            String base64Audio = responseProcessor.extractAudioAsBase64(response);
            log.debug(TTSConstants.RESPONSE_PROCESSED_LOG);

            log.info(TTSConstants.TTS_SUCCESS_LOG + " - {} caracteres Base64", base64Audio.length());
            return base64Audio;

        } catch (TtsException e) {
            log.error("Erro específico na síntese TTS Base64: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado na síntese TTS Base64", e);
            throw new TtsException("Erro inesperado durante síntese de voz para Base64", e);
        }
    }

    // Métodos adicionais para flexibilidade (opcionais)
    public byte[] synthesizeWithVoice(String text, String voice) throws TtsException {
        log.info("Síntese TTS com voz personalizada: {} - {} caracteres", voice, text != null ? text.length() : 0);

        try {
            String payload = payloadBuilder.buildSpeechPayload(text, voice);
            ResponseEntity<byte[]> response = httpClient.sendTTSRequest(payload);
            return responseProcessor.extractAudioBytes(response);
        } catch (Exception e) {
            log.error("Erro na síntese TTS com voz personalizada", e);
            throw new TtsException("Erro na síntese com voz: " + voice, e);
        }
    }

    public String synthesizeAsBase64WithVoice(String text, String voice) throws TtsException {
        byte[] audioBytes = synthesizeWithVoice(text, voice);
        return responseProcessor.convertToBase64(audioBytes);
    }
}