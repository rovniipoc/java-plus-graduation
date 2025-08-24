package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventSort;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PublicEventService {
    EventFullDto getEventById(long id, Long userId, HttpServletRequest request);

    Optional<Event> getEventFullById(long id);

    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  EventSort sort,
                                  int from,
                                  int size,
                                  HttpServletRequest request);

    List<EventShortDto> getRecommendations(Long userId, Integer maxResults);

    void likeEvent(Long eventId, Long userId);
}
