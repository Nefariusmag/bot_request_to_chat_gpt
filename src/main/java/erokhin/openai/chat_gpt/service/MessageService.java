package erokhin.openai.chat_gpt.service;

import erokhin.openai.chat_gpt.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
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

    /**
     * Method to send message that ChatGPT is switching context
     *
     * @param chatId The ID of the chat to send the message to
     * @throws InterruptedException If the thread is interrupted while waiting
     */
    public void sendSwitchContext(String chatId) throws InterruptedException {
        sendText(chatId, "ChatGPT забывает о чем был разговор");
    }

    /**
     * Method to send a message indicating that the chatGPT cannot understand the question.
     * Instructs the user to use a specific format when asking a question.
     *
     * @param chatId the ID of the chat to which the message will be sent
     * @throws InterruptedException if the thread is interrupted while sending the message
     */
    public void sendEmptyQuestion(String chatId) throws InterruptedException {
        sendText(chatId, "Я так не умею. Используйте формат: /ask 'ваш вопрос'");
    }

    /**
     * Method to send help message to the user
     *
     * @param chatId the unique identifier for the chat
     * @throws InterruptedException if the method is interrupted
     */
    public void sendHelpMessage(String chatId) throws InterruptedException {
        sendText(chatId, "Я могу переслать ваши вопросы ChatGPT, сохраняю контекст от вопроса к вопросу, но не очень долго. " +
                "\n\nЧтобы задать вопрос используйте формат:\n/ask 'ваш вопрос'" +
                "\nЕсли хотите очистить контекст, используйте команду:\n/new");
    }

    /**
     * Method to send a text message to a chat
     *
     * @param chatId The ID of the chat to send the message to
     * @param text   The text message to be sent
     * @throws InterruptedException If the thread is interrupted while sending the message
     */
    public void sendText(String chatId, String text) throws InterruptedException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(StringEscapeUtils.escapeHtml4(text));
        send(message);
    }

    /**
     * Private method to send a Telegram message
     *
     * @param message The SendMessage object containing the message details
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private void send(SendMessage message) throws InterruptedException {
        message.enableHtml(true);
        try {
            Message sentMessage = execute(message);
            log.debug("Message " + sentMessage + " sent to " + message.getChatId());
            botSleep();
        } catch (TelegramApiException e) {
            log.error("Exception: " + e);
            message.setText("Произошла ошибка, скорее всего в ответе ChatGPT были спец символы которые еще не научился экранировать");
            try {
                execute(message);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
