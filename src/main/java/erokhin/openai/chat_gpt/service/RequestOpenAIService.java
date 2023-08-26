package erokhin.openai.chat_gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import erokhin.openai.chat_gpt.tools.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestOpenAIService {

    @Value("${openchat.key}")
    private String openChatKey;

    @Value("${openchat.url}")
    private String openChatURL;

    private static final String DEFAULT_RESPONSE = "Нету ответа. Вероятно есть ошибка, попробуйте спросить иначе";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Sends a request to the Open Chat API with the specified question and returns the response.
     *
     * @param question the question to send to the Open Chat API
     * @return the response received from the Open Chat API
     */
    public String sendRequest(String question, String versionModel) {
        Optional<String> response = HttpClient.sendRequest(question, versionModel, openChatKey, openChatURL);
        return parseResponse(response);

    }

    private String parseResponse(Optional<String> response) {
        if (response.isEmpty()) {
            return DEFAULT_RESPONSE;
        } else {
            return parseResponseFromJson(response.get());
        }

    }

    private String parseResponseFromJson(String response) {
        String text = DEFAULT_RESPONSE;
        try {
            JsonNode jsonNode = mapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");

            if (choices.isArray() && !choices.isEmpty()) {
                for (JsonNode choice : choices) {
                    text = choice.get("message").get("content").asText();
                }
            }
            log.info("Parsed response");
        } catch (IOException e) {
            handleException(e);
            throw new RuntimeException(e);
        }
        return text;
    }

    private void handleException(IOException e) {
        log.info("Get error: " + e.getMessage());
    }

}
