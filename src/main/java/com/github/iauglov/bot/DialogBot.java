package com.github.iauglov.bot;

import com.github.iauglov.service.InteractiveProcessor;
import com.github.iauglov.service.MessageProcessor;
import im.dlg.botsdk.Bot;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class DialogBot {

    private final MessageProcessor messageProcessor;
    private final InteractiveProcessor interactiveProcessor;
    private final Bot bot;

    @PostConstruct
    public void postInit() {
        bot.messaging().onMessage(messageProcessor::process);
        bot.interactiveApi().onEvent(interactiveProcessor::process);
    }

}
