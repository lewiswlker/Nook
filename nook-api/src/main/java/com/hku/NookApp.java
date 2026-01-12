package com.hku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.hku.nook.service.websocket.WebSocketService;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class NookApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(NookApp.class, args);
        WebSocketService.setApplicationContext(app);
    }
}
