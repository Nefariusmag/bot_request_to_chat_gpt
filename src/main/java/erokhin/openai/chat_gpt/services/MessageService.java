package erokhin.openai.chat_gpt.services;

import erokhin.openai.chat_gpt.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class MessageService extends DefaultAbsSender {

    private final BotConfig botConfig;

    public MessageService(BotConfig botConfig) {
        super(new DefaultBotOptions());
        this.botConfig = botConfig;
    }
    private void BOT_SLEEP() throws InterruptedException {
        log.debug("Sleeping for 1 second");
        Thread.sleep(1000);
    }

    /**
     * Method to send message that chatGPT is thinking and have to wait
     *
     * @param chatId
     * @throws InterruptedException
     */
    public void sendThinking(String chatId) throws InterruptedException {
        sendText(chatId, "Подождите, ChatGPT думает...");
    }

    public void sendText(String chatId, String text) throws InterruptedException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        send(message);
    }

    private void send(SendMessage message) throws InterruptedException {
        message.enableHtml(true);
        try {
            Message sentMessage = execute(message);
            log.debug("Message " + sentMessage + " sent to " + message.getChatId());
            BOT_SLEEP();
        } catch (TelegramApiException e) {
            log.error("Exception: " + e);
        }
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
//        TODO fix this and use spring context
//        return botConfig.getToken();
    }
}
