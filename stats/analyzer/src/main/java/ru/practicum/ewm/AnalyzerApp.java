package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        UserActionKafkaListener userActionListener = context.getBean(UserActionKafkaListener.class);
        new Thread(userActionListener::startListen, "user-action-listener").start();

        EventSimilarityKafkaListener eventSimilarityKafkaListener = context.getBean(EventSimilarityKafkaListener.class);
        new Thread(eventSimilarityKafkaListener::startListen, "event-similarity-listener").start();
    }
}
