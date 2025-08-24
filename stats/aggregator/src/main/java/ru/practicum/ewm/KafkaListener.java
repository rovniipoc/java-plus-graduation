package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.service.UserActionProcessor;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListener {
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String userActionsTopic;
    private final UserActionProcessor userActionProcessor;

    public void startListen() {
        try {
            consumer.subscribe(List.of(userActionsTopic));
            log.info("Subscribed to topic: {}", userActionsTopic);
            while (true) {
                ConsumerRecords<String, UserActionAvro> userActions = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, UserActionAvro> userAction : userActions) {
                    try {
                        logReceivedRecord(userAction);
                        userActionProcessor.process(userAction.value());
                    } catch (Exception e) {
                        log.error("Exception while processing user actions: key: {}, value: {}",
                                userAction.key(), userAction.value(), e);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Exception while processing user actions", e);
        } finally {
            closeResources();
        }
    }

    private void logReceivedRecord(ConsumerRecord<String, UserActionAvro> record) {
        log.info("Received user action from Kafka: topic: {}, partition: {}, offset: {}, key: {}, timestamp: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.timestamp());
        log.debug("UserAction: {}", record.value());
    }

    private void closeResources() {
        try {
            producer.flush();
            log.info("All data sent to Kafka");
            consumer.commitSync();
            log.info("All offsets committed");
        } finally {
            log.info("Closing consumer");
            consumer.close();
            log.info("Closing producer");
            producer.close();
        }
    }
}
