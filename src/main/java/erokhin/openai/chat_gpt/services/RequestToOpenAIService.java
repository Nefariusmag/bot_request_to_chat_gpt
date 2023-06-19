package erokhin.openai.chat_gpt.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
@RequiredArgsConstructor
public class RequestToOpenAIService {

//    @Value("${openchat.key}")
    private String openChatKey = System.getenv("OPENAI_API_KEY");

    /**
     * Method to send POST request to OpenAI API used variable where can set body of request
     */
    public String sendRequest(String question) throws Exception {

        // TODO вынести в переменные
        URL url = new URL("https://api.openai.com/v1/chat/completions");

        // TODO вынести в отдельный класс и попробовать заменить на что-то более легковестное
        // Создаём соединение
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Устанавливаем метод запроса на POST и включаем возможность отправки данных
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Устанавливаем тип содержимого запроса
        connection.setRequestProperty("Content-Type", "application/json");

        // Устанавливаем токен
        // TODO Вынести в переменные в application.properties
        connection.setRequestProperty("Authorization", "Bearer " + openChatKey);

        // Подготавливаем данные для отправки
//        String jsonInputString = "{\n\"model\": \"gpt-3.5-turbo\",\n\"messages\": [{\"role\": \"system\", \"content\": \"" + question + "\"}, {\"role\": \"user\", \"content\": \"Hello!\"}]\n}";
        String jsonInputString = "{\"model\": \"gpt-3.5-turbo\",\"messages\": [{\"role\": \"user\", \"content\": \"" + question + "\"}]}";

        // Отправляем данные
        try(OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes();
            os.write(input, 0, input.length);
        }

        // Получаем и выводим ответ сервера
        System.out.println("Response Code: " + connection.getResponseCode());

        // Получаем содержимое ответа
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();

        // Выводим содержимое ответа
        String jsonResponse = content.toString();

        // Разбираем JSON-ответ в объекты Java с помощью Jackson
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        // Получаем и выводим определенные поля из JSON-ответа
        String text = "Нету ответа";
        JsonNode choices = jsonNode.get("choices");
        if (choices.isArray()) {
            for (JsonNode choice : choices) {
                text = choice.get("message").get("content").asText();
            }
        }

        return text;
    }
}
