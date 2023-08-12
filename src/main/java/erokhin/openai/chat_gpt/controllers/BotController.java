package erokhin.openai.chat_gpt.controllers;

import erokhin.openai.chat_gpt.config.BotConfig;
import erokhin.openai.chat_gpt.dto.VersionModel;
import erokhin.openai.chat_gpt.entity.QuestionEntity;
import erokhin.openai.chat_gpt.entity.UserEntity;
import erokhin.openai.chat_gpt.repository.QuestionRepository;
import erokhin.openai.chat_gpt.repository.UserRepository;
import erokhin.openai.chat_gpt.service.MessageService;
import erokhin.openai.chat_gpt.service.RequestOpenAIService;
import erokhin.openai.chat_gpt.tools.ChangeText;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    private final RequestOpenAIService requestOpenAIService;
    private final MessageService messageService;
    private final BotConfig botConfig;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final String versionModel = VersionModel.GPT_3_5_TURBO.getName();


    @Autowired
    public BotController(MessageService messageService, BotConfig botConfig,
                         QuestionRepository questionRepository,
                         UserRepository userRepository,
                         TelegramBotsApi telegramBotsApi,
                         RequestOpenAIService requestOpenAIService) throws TelegramApiException {
        this.messageService = messageService;
        this.botConfig = botConfig;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.requestOpenAIService = requestOpenAIService;
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


    /**
     * Handles the updates received by the bot.
     *
     * @param update The received update object.
     * @throws TelegramApiException if any error occurs while processing the update.
     */
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && hasMessage(update)) {
            String userId = update.getMessage().getChatId().toString();
            String messageText = update.getMessage().getText();

            if (messageText.contains("/start")) {
                processStartCommand(userId);
            } else if (messageText.contains("/new")) {
                processNewCommand(userId);
            } else if (messageText.contains("/ask")) {
                processAskCommand(userId, messageText);
            }
        }
    }

    private void processStartCommand(String userId) throws InterruptedException {
        messageService.sendHelpMessage(userId);
    }

    private void processNewCommand(String userId) throws InterruptedException {
        Long oldContextId = userRepository.findContextIdByUserId(userId);
        Long newContextId = (oldContextId != null) ? oldContextId + 1 : 1;
        saveUser(userId, newContextId);

        messageService.sendSwitchContext(userId);
    }

    private void saveUser(String userId, Long contextId) {
        userRepository.save(UserEntity.builder()
                .userId(userId)
                .contextId(contextId)
                .build());
    }

    @Transactional
    public void processAskCommand(String userId, String text) throws Exception {
        String question = text.replace("/ask ", "").replace("/ask", "");
        log.info("Question from: " + userId + " with text: " + question);

        if (question.isEmpty() || question.isBlank()) {
            messageService.sendEmptyQuestion(userId);
            return;
        }

        Long contextId = getContextIdAndSaveUserIfNecessary(userId);
        saveQuestion(userId, question, "user", contextId);
        handleQuestion(userId, question, contextId);
    }

    private Long getContextIdAndSaveUserIfNecessary(String userId) {
        Long contextId = userRepository.findContextIdByUserId(userId);
        if (contextId == null) {
            contextId = 1L;
            saveUser(userId, contextId);
        }
        return contextId;
    }

    private void saveQuestion(String userId, String text, String author, Long contextId) {
        questionRepository.save(QuestionEntity.builder()
                .userId(userId)
                .author(author)
                .contextId(contextId)
                .text(text)
                .datetime(LocalDateTime.now())
                .build());
    }

    private void handleQuestion(String userId, String question, Long contextId) throws Exception {
        List<QuestionEntity> historyByUserIdAndContextId =
                questionRepository.getHistoryByUserIdAndContextId(userId, contextId);
        List<QuestionEntity> shortHistoryList = getShortHistory(historyByUserIdAndContextId);
        String history = getJsonWithHistory(shortHistoryList);

        messageService.sendThinking(userId);
        log.info("Send request to OpenAI with question: " + question);

        String chatGPTResponse = requestOpenAIService.sendRequest(history, versionModel);
        log.info("Response from OpenAI: " + chatGPTResponse);

        saveQuestion(userId, chatGPTResponse, "assistant", contextId);
        messageService.sendText(userId, chatGPTResponse);
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
                    .append(ChangeText.getTextWhereAllSpecialSymbolsEscaped(questionEntity.getText()))
                    .append("\"},");
        }
        return history.substring(0, history.length() - 1);
    }

    private boolean hasMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
}
