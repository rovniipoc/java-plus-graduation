package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.message.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String userActionsTopic;

    //Маппинг -> Построение сообщения -> Логирование -> Отправка в Kafka -> Обработка ответа
    @Override
    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = mapToAvro(userActionProto);
        ProducerRecord<String, SpecificRecordBase> record = buildProducerRecord(userActionAvro);
        logProducerRecord(record);
        producer.send(record, this::handleCallback);
    }

    private UserActionAvro mapToAvro(UserActionProto userActionProto) {
        ActionTypeAvro actionTypeAvro = switch (userActionProto.getActionType()) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> throw new IllegalStateException("Unexpected value: " + userActionProto.getActionType());
        };
        UserActionAvro avro = UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(actionTypeAvro)
                .setTimestamp(Instant.ofEpochSecond(
                        userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .build();
        log.debug("Converted UserActionProto to UserActionAvro: {}", avro);
        return avro;
    }

    private ProducerRecord<String, SpecificRecordBase> buildProducerRecord(UserActionAvro userActionAvro) {
        return new ProducerRecord<>(
                userActionsTopic,
                null,
                userActionAvro.getTimestamp().toEpochMilli(),
                null,
                userActionAvro
        );
    }

    private void logProducerRecord(ProducerRecord<String, SpecificRecordBase> producerRecord) {
        log.info("Send ProducerRecord: topic={}, key={}, partition={}, timestamp={}",
                producerRecord.topic(),
                producerRecord.key(),
                producerRecord.partition() != null ? producerRecord.partition() : "Auto partition assignment",
                producerRecord.timestamp() != null ? producerRecord.timestamp() : "Not set");
        log.debug("ProducerRecord: {}", producerRecord);
    }

    private void handleCallback(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            log.error("Error sending message to Kafka: {}", exception.getMessage(), exception);
        } else {
            log.info("Message sent to Kafka: topic={}, offset={}", metadata.topic(), metadata.offset());
        }
    }
}
