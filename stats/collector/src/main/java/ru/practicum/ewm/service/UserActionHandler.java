package ru.practicum.ewm.service;

import ru.practicum.ewm.stats.message.UserActionProto;

public interface UserActionHandler {
    void handle(UserActionProto userActionProto);
}
