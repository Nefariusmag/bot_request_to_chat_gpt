package erokhin.openai.chat_gpt.controllers;

import erokhin.openai.chat_gpt.dto.RequestQuestion;
import erokhin.openai.chat_gpt.service.RequestToOpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//TODO read how to create api right way
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AskChatController {
    private final RequestToOpenAIService requestToOpenAIService;

    @PostMapping("/ask-chat")
    public String askChat(@RequestBody RequestQuestion question) throws Exception {
        log.info("Get question: " + question.getQuestion());
        String response = requestToOpenAIService.sendRequest(question.getQuestion());
        log.info("Response: " + response);
        return response;
    }

}
