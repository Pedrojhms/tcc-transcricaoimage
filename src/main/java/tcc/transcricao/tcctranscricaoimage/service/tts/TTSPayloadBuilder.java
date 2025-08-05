package tcc.transcricao.tcctranscricaoimage.service.tts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.TTSConstants;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;

@Component
@Slf4j
public class TTSPayloadBuilder {

    public String buildSpeechPayload(String text) throws TtsException {
        return buildSpeechPayload(text, TTSConstants.DEFAULT_VOICE);
    }

    public String buildSpeechPayload(String text, String voice) throws TtsException {
        return buildSpeechPayload(text, voice, TTSConstants.TTS_MODEL);
    }

    public String buildSpeechPayload(String text, String voice, String model) throws TtsException {
        validateInput(text, voice, model);

        log.debug("Construindo payload TTS - Modelo: {}, Voz: {}, Texto: {} chars",
                model, voice, text.length());

        String escapedText = escapeJsonString(text);

        String payload = String.format(
                "{" +
                        "\"model\": \"%s\"," +
                        "\"input\": \"%s\"," +
                        "\"voice\": \"%s\"" +
                        "}",
                model, escapedText, voice
        );

        log.debug("Payload TTS construído com sucesso - {} caracteres", payload.length());
        return payload;
    }

    private void validateInput(String text, String voice, String model) throws TtsException {
        validateText(text);
        validateVoice(voice);
        validateModel(model);
    }

    private void validateText(String text) throws TtsException {
        if (text == null) {
            throw new TtsException(TTSConstants.INVALID_TEXT_ERROR + ": texto é null");
        }

        if (text.trim().isEmpty()) {
            throw new TtsException(TTSConstants.EMPTY_TEXT_ERROR);
        }

        if (text.length() > TTSConstants.MAX_TEXT_LENGTH) {
            throw new TtsException(TTSConstants.TEXT_TOO_LONG_ERROR +
                    ". Texto atual: " + text.length() + " caracteres");
        }

        if (text.length() < TTSConstants.MIN_TEXT_LENGTH) {
            throw new TtsException("Texto muito curto para síntese");
        }
    }

    private void validateVoice(String voice) throws TtsException {
        if (voice == null || voice.trim().isEmpty()) {
            throw new TtsException("Voz não pode ser vazia");
        }

        // Validação das vozes disponíveis
        String[] availableVoices = {
                TTSConstants.VOICE_ALLOY, TTSConstants.VOICE_ECHO, TTSConstants.VOICE_FABLE,
                TTSConstants.VOICE_ONYX, TTSConstants.VOICE_NOVA, TTSConstants.VOICE_SHIMMER
        };

        boolean isValidVoice = false;
        for (String availableVoice : availableVoices) {
            if (availableVoice.equals(voice)) {
                isValidVoice = true;
                break;
            }
        }

        if (!isValidVoice) {
            throw new TtsException("Voz inválida: " + voice + ". Vozes disponíveis: " +
                    String.join(", ", availableVoices));
        }
    }

    private void validateModel(String model) throws TtsException {
        if (model == null || model.trim().isEmpty()) {
            throw new TtsException("Modelo TTS não pode ser vazio");
        }

        // Validação do modelo (pode ser expandida conforme novos modelos)
        if (!TTSConstants.TTS_MODEL.equals(model)) {
            log.warn("Modelo TTS não padrão sendo usado: {}", model);
        }
    }

    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }

        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}