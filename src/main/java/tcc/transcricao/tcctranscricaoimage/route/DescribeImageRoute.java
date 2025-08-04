package tcc.transcricao.tcctranscricaoimage.route;

import org.apache.camel.builder.RouteBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tcc.transcricao.tcctranscricaoimage.exception.DescricaoImagemException;
import tcc.transcricao.tcctranscricaoimage.exception.TtsException;
import tcc.transcricao.tcctranscricaoimage.model.PerformanceMetric;
import tcc.transcricao.tcctranscricaoimage.processor.ExceptionToHttpResponseProcessor;
import tcc.transcricao.tcctranscricaoimage.repository.PerformanceMetricRepository;
import tcc.transcricao.tcctranscricaoimage.service.ImageDescriptionService;
import tcc.transcricao.tcctranscricaoimage.service.TtsService;

import java.time.LocalDateTime;

@Component
public class DescribeImageRoute extends RouteBuilder {

    @Autowired
    private ImageDescriptionService imageDescriptionService;
    @Autowired
    private TtsService ttsService;
    @Autowired
    private ExceptionToHttpResponseProcessor exceptionToHttpResponseProcessor;
    @Autowired
    private PerformanceMetricRepository metricRepository;

    @Override
    public void configure() {

        onException(DescricaoImagemException.class)
                .handled(true)
                .process(exceptionToHttpResponseProcessor);

        onException(TtsException.class)
                .handled(true)
                .process(exceptionToHttpResponseProcessor);

        from("rest:POST:/whatsapp-webhook")
                .routeId("whatsapp-webhook-route")
                .log("Webhook do whatsapp recebido!")
                .process(exchange -> {
                    exchange.setProperty("startTime", System.currentTimeMillis());
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject webhookJson = new JSONObject(body);
                    String phone = webhookJson.getString("from");
                    String imageBase64 = webhookJson.getJSONObject("media").getString("data");
                    exchange.setProperty("phone", phone);
                    exchange.setProperty("imageBase64", imageBase64);
                })
                .wireTap("direct:send-confirmation")
                .to("direct:process-image-and-audio")
                .to("direct:send-whatsapp-voice")
                .to("direct:detail-db-metrics")
                .log("Métricas salvas na base de dados!")
                .setBody(constant("OK"));

        from("direct:detail-db-metrics")
                .log("Iniciando log DB métricas")
                .process(exchange -> {
                    long start = (long) exchange.getProperty("startTime");
                    long desc = (long) exchange.getProperty("descTime");
                    long tts = (long) exchange.getProperty("ttsTime");
                    long send = System.currentTimeMillis();
                    String phone = (String) exchange.getProperty("phone");

                    long tempoDescricao = desc - start;
                    long tempoTts = tts - desc;
                    long tempoEnvio = send - tts;
                    long tempoTotal = send - start;

                    // Log detalhado
//                    log.info("Tempo descrição: {}ms", tempoDescricao);
//                    log.info("Tempo TTS: {}ms", tempoTts);
//                    log.info("Tempo envio: {}ms", tempoEnvio);
//                    log.info("Tempo total: {}ms", tempoTotal);

                    // Salva métrica
                    PerformanceMetric metric = new PerformanceMetric();
                    metric.setTempoDescricao(tempoDescricao);
                    metric.setTempoTts(tempoTts);
                    metric.setTempoEnvio(tempoEnvio);
                    metric.setTempoTotal(tempoTotal);
                    metric.setPhone(phone);
                    metric.setData(LocalDateTime.now());
                    metricRepository.save(metric);
                });

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

        from("direct:process-image-and-audio")
                .log("Iniciando processamento de imagem e síntese de áudio")
                .process(exchange -> {
                    // Gera descrição da imagem
                    String imageBase64 = (String) exchange.getProperty("imageBase64");
                    String descricao = imageDescriptionService.getDescription(imageBase64);
                    exchange.setProperty("descricao", descricao);
                })
                .log("Descrição gerada!")
                .process(exchange -> {
                    String descricao = (String) exchange.getProperty("descricao");
                    String audioBase64 = ttsService.synthesizeAsBase64(descricao);
                    exchange.setProperty("audioBase64", audioBase64);
                    exchange.setProperty("descTime", System.currentTimeMillis());
                })
                .log("Áudio gerado em base64!");

        from("direct:send-whatsapp-voice")
                .log("Enviando áudio para o Whatsapp /sendVoice")
                .process(exchange -> {
                    String to = (String) exchange.getProperty("phone");
                    String audioBase64 = (String) exchange.getProperty("audioBase64");
                    JSONObject payload = new JSONObject();
                    payload.put("to", to);
                    payload.put("audioBase64", audioBase64);
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(payload.toString());
                    exchange.setProperty("ttsTime", System.currentTimeMillis());
                })
                .to("http://whatsapp:3000/sendVoice?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }
}