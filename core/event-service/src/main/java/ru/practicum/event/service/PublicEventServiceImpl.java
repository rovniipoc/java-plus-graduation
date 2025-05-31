package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventSort;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.ewm.AnalyzerClient;
import ru.practicum.ewm.CollectorClient;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    @Value("${app.name:ewm-main}") // "ewm-main" — значение по умолчанию
    private String APP_NAME;

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(long id, Long userId, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event c id " + id + "не найден"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event c id " + id + "еще не опубликован");
        }

        collectorClient.sendEventView(userId, id);

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        log.info("получен eventFullDto с ID = {}", eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventFullById(long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         int from,
                                         int size,
                                         HttpServletRequest request) {

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(1);

        if (end.isBefore(start)) {
            throw new BadRequestException("Недопустимый временной промежуток, время окончание поиска не может быть раньше времени начала поиска");
        }


        PageRequest page = PageRequest.of(from, size);
        Page<Event> pageEvents;
        if (onlyAvailable) {
            pageEvents = eventRepository.findAllByPublicFiltersAndOnlyAvailable(text, categories, paid, start, end, page);
        } else {
            pageEvents = eventRepository.findAllByPublicFilters(text, categories, paid, start, end, page);
        }

        List<Event> events = pageEvents.getContent();

        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Event event : events) {
            EventShortDto dto = eventMapper.toEventShortDto(event);
            eventShortDtos.add(dto);
        }

        if (sort != null) {
            if (sort.equals(EventSort.EVENT_DATE)) {
                eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate));
            }
        }

        return eventShortDtos;
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, Integer maxResults) {
        return analyzerClient.getRecommendations(userId, maxResults).stream()
                .sorted((a, b) -> (int) (a.getScore() - b.getScore()))
                .map((r) -> {
                    Event event = eventRepository.findById(r.getEventId())
                            .orElseThrow(() -> new NotFoundException("Event c id " + r.getEventId() + "не найден"));
                    return eventMapper.toEventShortDto(event);
                }).toList();
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event c id " + eventId + "не найден"));
        collectorClient.sendEventLike(userId, eventId);
    }

//    private void addHit(HttpServletRequest request) {
//        EndpointHitInputDto hit = new EndpointHitInputDto();
//        hit.setApp(APP_NAME);
//        hit.setUri(request.getRequestURI());
//        hit.setIp(request.getRemoteAddr());
//        hit.setTimestamp(LocalDateTime.now());
//        statsClient.addHit(hit);
//    }

//    private Event updateEventViewsInRepository(Event event) {
//
//        try {
//            Long eventId = event.getId();
//            String eventUri = "/events/" + eventId;
//            ResponseEntity<Object> responseEntity = statsClient.getStats(LocalDateTime.now().minusYears(999), LocalDateTime.now().plusYears(1), List.of(eventUri), true);
//
//            if (responseEntity.getStatusCode().is2xxSuccessful()) {
//                ObjectMapper objectMapper = new ObjectMapper();
//                List<Map<String, Object>> responseBody = objectMapper.convertValue(responseEntity.getBody(), new TypeReference<List<Map<String, Object>>>() {
//                });
//
//                List<ViewStatsOutputDto> responseList = responseBody.stream()
//                    .map(map -> new ViewStatsOutputDto((String) map.get("app"), (String) map.get("uri"), ((Number) map.get("hits")).longValue()))
//                    .toList();
//
//                if (!responseList.isEmpty()) {
//                    ViewStatsOutputDto viewStatsOutputDto = responseList.getFirst();
//                    event.setViews(viewStatsOutputDto.getHits());
//                    return eventRepository.save(event);
//                }
//            }
//            return event;
//        } catch (Exception e) {
//            return event;
//        }
//    }


}
