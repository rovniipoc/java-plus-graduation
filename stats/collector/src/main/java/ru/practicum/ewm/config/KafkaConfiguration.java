package ru.practicum.ewm.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {
    private final KafkaProperties kafkaProperties;
    private KafkaProducer<String, SpecificRecordBase> producer;

    @Bean
    KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        if (producer == null) {
            Properties config = new Properties();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServer());
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
            producer = new KafkaProducer<>(config);
        }
        return producer;
    }

    @Bean
    public String userActionsTopic() {
        return kafkaProperties.getTopic();
    }

    @PreDestroy
    public void closeProducer() {
        if (producer != null) {
            producer.close();
        }
    }
}