package tcc.transcricao.tcctranscricaoimage;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TccTranscricaoimageApplication {

    public static void main(String[] args) {
        Brotli4jLoader.ensureAvailability();
        SpringApplication.run(TccTranscricaoimageApplication.class, args);
    }

}
