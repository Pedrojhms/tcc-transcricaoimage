package tcc.transcricao.tcctranscricaoimage.route;

import org.apache.camel.builder.RouteBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;
import tcc.transcricao.tcctranscricaoimage.processor.ExceptionToHttpResponseProcessor;
import tcc.transcricao.tcctranscricaoimage.service.AudioStorageService;
import tcc.transcricao.tcctranscricaoimage.service.ImageDescriptionService;
import tcc.transcricao.tcctranscricaoimage.service.TtsService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class DescribeImageRoute extends RouteBuilder {

    @Autowired
    private ImageDescriptionService imageDescriptionService;
    @Autowired
    private TtsService ttsService;
    @Autowired
    private AudioStorageService audioStorageService;
    @Autowired
    private ExceptionToHttpResponseProcessor exceptionToHttpResponseProcessor;

    @Override
    public void configure() {

        onException(DescricaoImagemException.class)
                .handled(true)
                .process(exceptionToHttpResponseProcessor);

        onException(TtsException.class)
                .handled(true)
                .process(exceptionToHttpResponseProcessor);

        from("rest:POST:/receive-image")
                .routeId("image-description-route")
                .log("Imagem recebida via /receive-image")
                .to("direct:process-image");

        from("rest:POST:/whatsapp-webhook")
                .routeId("whatsapp-webhook-route")
                .log("Webhook do whatsapp recebido!")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject webhookJson = new JSONObject(body);

                    String phone = webhookJson.getString("from");

                    String image = webhookJson.getJSONObject("media").getString("data");

                    exchange.setProperty("phone", phone);
                    exchange.getIn().setBody(image);
                })
                .log("Respondendo a confirmação do recebimento!")
                .wireTap("direct:send-confirmation")
                .to("direct:process-image")
                .process(exchange -> {
                    byte[] audio = Files.readAllBytes(Paths.get(exchange.getProperty("pathAudio").toString())); // Agora sim, lê o arquivo pra base64
                    String audioBase64 = Base64.getEncoder().encodeToString(audio);

                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(audioBase64);
                })
                .to("direct:send-whatsapp-voice")
                .setBody(constant("OK"));

        from("direct:send-confirmation")
                .process(exchange -> {
                    String chatId = (String) exchange.getProperty("phone");
                    JSONObject payload = new JSONObject();
                    payload.put("to", chatId);
                    payload.put("message", "Imagem recebida com sucesso! Estamos processando sua solicitação.");
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(payload.toString());
                })
                .to("http://whatsapp:3000/sendText?bridgeEndpoint=true&throwExceptionOnFailure=false");

        from("direct:process-image")
                .log("Iniciando processamento de imagem")
                .process(exchange -> {
                    String base64 = exchange.getIn().getBody(String.class);
                    String descricao = imageDescriptionService.getDescription(base64);
                    exchange.getIn().setBody(descricao);
                })
                .log("Descrição gerada!")
                .process(exchange -> {
                    String descricao = exchange.getIn().getBody(String.class);
                    byte[] audio = ttsService.synthesize(descricao);
                    exchange.getIn().setBody(audio);
                })
                .log("Audio gerado!")
                .process(exchange -> {
                    byte[] audio = exchange.getIn().getBody(byte[].class);
                    String pathAudio = audioStorageService.saveAudio(audio);
                    exchange.setProperty("pathAudio", pathAudio);
                    exchange.getIn().setBody("Arquivo salvo: " + pathAudio);
                })
                .log("Arquivo salvo!");

        from("direct:send-whatsapp-voice")
                .log("Enviando áudio para o Whatsapp /sendVoice")
                .process(exchange -> {
                    String to = (String) exchange.getProperty("phone");
                    String audioBase64 = exchange.getIn().getBody(String.class);

                    JSONObject payload = new JSONObject();
                    payload.put("to", to);
                    payload.put("audioBase64", audioBase64);

                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(payload.toString());
                })
                .to("http://whatsapp:3000/sendVoice?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }
}