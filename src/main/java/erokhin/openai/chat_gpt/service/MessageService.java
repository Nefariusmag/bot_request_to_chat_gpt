package erokhin.openai.chat_gpt.service;

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
    private final Long authorChatId;

    public MessageService(BotConfig botConfig) {
        super(new DefaultBotOptions());
        this.botConfig = botConfig;
        this.authorChatId = botConfig.getAuthorChatId();
    }

    private void botSleep() throws InterruptedException {
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

    public void sendSwitchContext(String chatId) throws InterruptedException {
        sendText(chatId, "ChatGPT забывает о чем был разговор");
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
            botSleep();
        } catch (TelegramApiException e) {
            log.error("Exception: " + e);
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
