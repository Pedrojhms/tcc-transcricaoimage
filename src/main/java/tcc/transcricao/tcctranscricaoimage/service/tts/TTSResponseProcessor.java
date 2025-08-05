package tcc.transcricao.tcctranscricaoimage.service.tts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.constants.TTSConstants;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;

import java.util.Base64;

@Component
@Slf4j
public class TTSResponseProcessor {

    public byte[] extractAudioBytes(ResponseEntity<byte[]> response) throws TtsException {
        try {
            log.debug("Processando resposta TTS");

            byte[] audioBytes = response.getBody();
            validateAudioBytes(audioBytes);

            log.info("Áudio TTS extraído com sucesso - {} bytes", audioBytes.length);
            return audioBytes;

        } catch (Exception e) {
            log.error("Erro ao processar resposta TTS", e);
            throw new TtsException(TTSConstants.AUDIO_PROCESSING_ERROR, e);
        }
    }

    public String convertToBase64(byte[] audioBytes) throws TtsException {
        try {
            validateAudioBytes(audioBytes);

            log.debug("Convertendo áudio para Base64");
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

            log.info("Conversão Base64 concluída - {} bytes -> {} caracteres",
                    audioBytes.length, base64Audio.length());

            return base64Audio;

        } catch (Exception e) {
            log.error("Erro ao converter áudio para Base64", e);
            throw new TtsException("Erro na conversão Base64 do áudio TTS", e);
        }
    }

    public String extractAudioAsBase64(ResponseEntity<byte[]> response) throws TtsException {
        byte[] audioBytes = extractAudioBytes(response);
        return convertToBase64(audioBytes);
    }

    private void validateAudioBytes(byte[] audioBytes) throws TtsException {
        if (audioBytes == null) {
            throw new TtsException("Dados de áudio são null");
        }

        if (audioBytes.length == 0) {
            throw new TtsException("Dados de áudio estão vazios");
        }

        // Validação básica do formato de áudio (verificação de header)
        validateAudioFormat(audioBytes);

        log.debug("Dados de áudio validados - {} bytes", audioBytes.length);
    }

    private void validateAudioFormat(byte[] audioBytes) throws TtsException {
        // Verificação básica de formato de áudio comum
        // OpenAI TTS geralmente retorna MP3
        if (audioBytes.length < 4) {
            throw new TtsException("Arquivo de áudio muito pequeno para ter formato válido");
        }

        // Verificação de header MP3 (ID3v2 ou frame sync)
        boolean hasID3v2 = audioBytes[0] == 'I' && audioBytes[1] == 'D' && audioBytes[2] == '3';
        boolean hasMPEGSync = (audioBytes[0] & 0xFF) == 0xFF && (audioBytes[1] & 0xE0) == 0xE0;

        if (!hasID3v2 && !hasMPEGSync) {
            log.warn("Formato de áudio pode não ser MP3 padrão. Headers: {} {} {} {}",
                    String.format("%02X", audioBytes[0]),
                    String.format("%02X", audioBytes[1]),
                    String.format("%02X", audioBytes[2]),
                    String.format("%02X", audioBytes[3]));
        }

        log.debug("Formato de áudio validado");
    }
}