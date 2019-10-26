package com.github.iauglov;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Value("${dchat.bot.token}")
    private String botToken;
    @Value("${dchat.bot.host}")
    private String botHost;
    @Value("${dchat.bot.port}")
    private Integer botPort;

    @Bean
    public Bot bot() throws ExecutionException, InterruptedException {
        BotConfig botConfig = BotConfig.Builder.aBotConfig()
                .withHost(botHost)
                .withPort(botPort)
                .withToken(botToken)
                .build();

        return Bot.start(botConfig).get();
    }


}
