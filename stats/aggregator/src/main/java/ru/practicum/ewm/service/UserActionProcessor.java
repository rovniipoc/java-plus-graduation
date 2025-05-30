package ru.practicum.ewm.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionProcessor {
    void process(UserActionAvro userAction);
}
