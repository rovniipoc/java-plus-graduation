package ru.practicum.ewm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
public class KafkaProperties {
    private String bootstrapServer;
    private String topic;
    private Integer retriesCount;
    private Long retryBackOffMs;
    private Boolean isIdempotence;
    private Long batchSize;
    private Long lingerMs;
}