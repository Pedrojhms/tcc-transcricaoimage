package tcc.transcricao.tcctranscricaoimage.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import tcc.transcricao.tcctranscricaoimage.service.SatisfactionSurveyService;

@Component
public class SatisfactionSurveyRoute extends RouteBuilder {

    private static final String URL_SEND_MENSAGE = "http://whatsapp:3000/sendText?bridgeEndpoint=true&throwExceptionOnFailure=false";

    @Autowired
    private SatisfactionSurveyService surveyService;

    @Override
    public void configure() throws Exception {

        // Inicia o questionário: envia a primeira pergunta via WhatsApp
        from("direct:start-survey")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject json = new JSONObject(body);
                    String userPhone = json.getString("phone");
                    String imageId = json.getString("imageId");

                    JSONObject payload = new JSONObject();
                    payload.put("to", userPhone);
                    payload.put("message", getQuestionText(1));
                    payload.put("imageId", imageId);

                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(payload.toString());
                })
                .to(URL_SEND_MENSAGE)
                .log("Primeira pergunta do questionário enviada.");

        // Recebe resposta do questionário, salva e retorna próxima pergunta OU finaliza
        from("rest:POST:/whatsapp-survey")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject json = new JSONObject(body);

                    String userPhone = json.getString("from");
                    String imageId = json.getString("imageId");
                    int questionNumber = json.getInt("questionNumber");
                    int score = json.getInt("score");

                    surveyService.saveAnswer(userPhone, imageId, questionNumber, score);

                    JSONObject response = new JSONObject();
                    if (questionNumber < 5) {
                        String nextQuestion = getQuestionText(questionNumber + 1);
                        response.put("message", nextQuestion);
                        response.put("finished", false);
                    } else {
                        response.put("message", "✅ Obrigado por responder ao questionário!");
                        response.put("finished", true);
                    }
                    // Retorna para o Node.js (que envia ao usuário)
                    exchange.getIn().setHeader("Content-Type", "application/json");
                    exchange.getIn().setBody(response.toString());
                })
                .log("Resposta processada e próxima pergunta retornada.");
    }

    // Retorna o texto da pergunta conforme o número
    private String getQuestionText(int n) {
        return switch (n) {
            case 1 -> """
                     ⏱️ *Pergunta 1/5*
                     *Como você avalia o tempo que a aplicação levou para processar sua imagem?*

                     1️⃣ Muito lento
                     2️⃣ Lento
                     3️⃣ Razoável
                     4️⃣ Rápido
                     5️⃣ Muito rápido

                     📝 Digite sua resposta (1-5):""";
            case 2 -> """
                     🎯 *Pergunta 2/5*
                     *Qual sua satisfação com a qualidade da descrição da imagem obtida?*

                     1️⃣ Muito insatisfeito
                     2️⃣ Insatisfeito
                     3️⃣ Neutro
                     4️⃣ Satisfeito
                     5️⃣ Muito satisfeito

                     📝 Digite sua resposta (1-5):""";
            case 3 -> """
                     🎯 *Pergunta 3/5*
                     *O quão precisa você considera a descrição gerada?*

                     1️⃣ Nada precisa
                     2️⃣ Pouco precisa
                     3️⃣ Razoavelmente precisa
                     4️⃣ Muito precisa
                     5️⃣ Extremamente precisa

                     📝 Digite sua resposta (1-5):""";
            case 4 -> """
                     🎯 *Pergunta 4/5*
                     *Como você avalia a facilidade de uso da aplicação?*

                     1️⃣ Muito difícil
                     2️⃣ Difícil
                     3️⃣ Razoável
                     4️⃣ Fácil
                     5️⃣ Muito fácil

                     📝 Digite sua resposta (1-5):""";
            case 5 -> """
                     🎯 *Pergunta 5/5*
                     *Qual sua satisfação geral com o resultado obtido?*

                     1️⃣ Muito insatisfeito
                     2️⃣ Insatisfeito
                     3️⃣ Neutro
                     4️⃣ Satisfeito
                     5️⃣ Muito satisfeito

                     📝 Digite sua resposta (1-5):""";
            default -> "";
        };
    }
}