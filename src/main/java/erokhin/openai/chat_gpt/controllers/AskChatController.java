package erokhin.openai.chat_gpt.controllers;

import erokhin.openai.chat_gpt.dto.RequestQuestion;
import erokhin.openai.chat_gpt.services.RequestToOpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

//TODO read how to create api right way
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AskChatController {
    private final RequestToOpenAIService requestToOpenAIService;

    @PostMapping("/ask-chat")
    public String askChat(@RequestBody RequestQuestion question) throws Exception {
        return requestToOpenAIService.sendRequest(question.getQuestion());
    }

}
