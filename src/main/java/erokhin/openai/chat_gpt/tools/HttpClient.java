package erokhin.openai.chat_gpt.tools;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClient {
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .build();

    public static Optional<String> sendRequest(String question, String versionModel, String openChatKey, String openChatURL) {
        RequestBody body = RequestBody.create(JSON, "{\"model\": \"" + versionModel + "\",\"messages\": [" + question + "]}");
        Request request = new Request.Builder()
                .url(openChatURL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + openChatKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return Optional.of(response.body().string());
        } catch (IOException e) {
            handleException(e);
            throw new RuntimeException(e);
        }
    }

    private static void handleException(IOException e) {
        log.info("Get error: " + e.getMessage());
    }
}
