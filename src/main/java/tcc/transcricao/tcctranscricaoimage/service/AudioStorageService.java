package tcc.transcricao.tcctranscricaoimage.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AudioStorageService {

    public String saveAudio(byte[] audio) throws Exception {

        if (audio == null || audio.length == 0) {
            throw new RuntimeException("Áudio vazio retornado pelo TTS");
        }

        String fileName = "audio_" + System.currentTimeMillis() + ".mp3";
        Path outputDir = Paths.get("audios");
        try {
            Files.createDirectories(outputDir);
            Path outputPath = outputDir.resolve(fileName);
            Files.write(outputPath, audio);

            return outputPath.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao salvar arquivo de áudio", e);
        }
    }
}