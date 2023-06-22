package erokhin.openai.chat_gpt.controllers;

import erokhin.openai.chat_gpt.config.BotConfig;
import erokhin.openai.chat_gpt.entity.QuestionEntity;
import erokhin.openai.chat_gpt.entity.UserEntity;
import erokhin.openai.chat_gpt.repository.QuestionRepository;
import erokhin.openai.chat_gpt.repository.UserRepository;
import erokhin.openai.chat_gpt.service.MessageService;
import erokhin.openai.chat_gpt.service.RequestToOpenAIService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class BotController extends TelegramLongPollingBot {

    private final RequestToOpenAIService requestToOpenAIService;
    private final MessageService messageService;
    private final BotConfig botConfig;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Autowired
    public BotController(RequestToOpenAIService requestToOpenAIService,
                         MessageService messageService, BotConfig botConfig,
                         QuestionRepository questionRepository,
                         UserRepository userRepository,
                         TelegramBotsApi telegramBotsApi) throws TelegramApiException {
        this.requestToOpenAIService = requestToOpenAIService;
        this.messageService = messageService;
        this.botConfig = botConfig;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        telegramBotsApi.registerBot(this);
    }


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();

    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows //TODO read about this annotation
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && hasMessage(update)) {

            String userId = update.getMessage().getChatId().toString();

            if (update.getMessage().getText().contains("/new")) {
                Long oldContextId = userRepository.findContextIdByUserId(userId);
                Long newContextId = oldContextId != null ? oldContextId + 1L : 1L;
                userRepository.save(UserEntity.builder()
                        .userId(userId)
                        .contextId(newContextId)
                        .build());
                messageService.sendSwitchContext(userId);
            }
            if (update.getMessage().getText().contains("/ask")) {

                String question = update.getMessage().getText().replace("/ask ", "")
                        .replace("/ask", "");
                log.info("Question from: " + userId + " with text: " + question);

                Long contextId = userRepository.findContextIdByUserId(userId);
                if (contextId == null) {
                    contextId = 1L;
                    userRepository.save(UserEntity.builder()
                            .userId(userId)
                            .contextId(contextId)
                            .build());
                }

                questionRepository.save(QuestionEntity.builder()
                        .userId(userId)
                        .author("user")
                        .contextId(contextId)
                        .text(question)
                        .datetime(LocalDateTime.now())
                        .build());

                List<QuestionEntity> historyByUserIdAndContextId = questionRepository.getHistoryByUserIdAndContextId(userId, contextId);
                List<QuestionEntity> shortHistoryList = getShortHistory(historyByUserIdAndContextId);
                String history = getJsonWithHistory(shortHistoryList);

                messageService.sendThinking(userId);
                log.info("Send request to OpenAI with question: " + question);

                String chatGPTResponse = requestToOpenAIService.sendRequest(history);
                log.info("Response from OpenAI: " + chatGPTResponse);
                questionRepository.save(QuestionEntity.builder()
                        .userId(userId)
                        .author("assistant")
                        .contextId(contextId)
                        .text(chatGPTResponse)
                        .datetime(LocalDateTime.now())
                        .build());
                messageService.sendText(userId, chatGPTResponse);

            }
        }
    }

    /**
     * From List<QuestionEntity> list which sorted from old to new
     * get new list where sum of length of all text less than 1024
     * if sum of length of all text more than 1024, have to get only new messages
     */
    private List<QuestionEntity> getShortHistory(List<QuestionEntity> list) {
        int totalLength = 0;
        Iterator<QuestionEntity> iter = list.iterator();
        while (iter.hasNext()) {
            QuestionEntity s = iter.next();
            totalLength += s.getText().length();
            if (totalLength > 2048) {
                iter.remove();
                totalLength -= s.getText().length();
            }
        }
        return list;
    }

    /**
     * get json with history, with structure:
     * {"role": author, "content": text},{...}
     * from List<QuestionEntity> list
     */
    private String getJsonWithHistory(List<QuestionEntity> list) {
        StringBuilder history = new StringBuilder();
        for (QuestionEntity questionEntity : list) {
            history.append("{\"role\": \"")
                    .append(questionEntity.getAuthor())
                    .append("\", \"content\": \"")
                    .append(getTextWhereAllSpecialSymbolsEscaped(questionEntity.getText()))
                    .append("\"},");
        }
        return history.substring(0, history.length() - 1);
    }

    private String getTextWhereAllSpecialSymbolsEscaped(String text){
        return text.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace(":", "\\\\:");
    }

    private boolean hasMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
}
