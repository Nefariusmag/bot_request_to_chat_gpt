package erokhin.openai.chat_gpt.telegram_bot;

import erokhin.openai.chat_gpt.config.BotConfig;
import erokhin.openai.chat_gpt.services.MessageService;
import erokhin.openai.chat_gpt.services.RequestToOpenAIService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

//    private final RequestToOpenAIService requestToOpenAIService;

    private BotConfig botConfig;

//    @Autowired
//    private MessageService messageService;

//    @Autowired // TODO read about this annotation
//    public Bot() {
//        this.botConfig = botConfig;
//        this.messageService = messageService;
//    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_NAME");
//        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
//        return botConfig.getToken();
    }

    @SneakyThrows //TODO read about this annotation
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().contains("/ask")) {
                RequestToOpenAIService requestToOpenAIService = new RequestToOpenAIService();
                log.info("Question from: " + update.getMessage().getChatId() + " with text: " + update.getMessage().getText());
                String question = update.getMessage().getText().replace("/ask", "");
                MessageService messageService = new MessageService(botConfig);
                messageService.sendThinking(update.getMessage().getChatId().toString());
                log.info("Send request to OpenAI with question: " + question);
                String chatGPTResponse = requestToOpenAIService.sendRequest(question);
                log.info("Response from OpenAI: " + chatGPTResponse);
                messageService.sendText(update.getMessage().getChatId().toString(), chatGPTResponse);
            }
        }
    }

    private boolean hasMessage(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }
}
