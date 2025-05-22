package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.RequestServiceClient;
import ru.practicum.feign.UserServiceClient;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;
    private final CategoryRepository categoryRepository;
    private final RequestServiceClient requestServiceClient;

    private static final long HOURS_BEFORE_EVENT = 2;

    public List<EventShortDto> getAllEventsOfUser(Long userId, int from, int size) {

        getUserOrThrow(userId);
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Event> eventPage = eventRepository.findAllByInitiatorId(userId, pageRequest);

        return eventPage.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User initiator = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());

        checkEventDate(dto.getEventDate());
        Event event = EventMapper.toEvent(dto, initiator, category);
        Event saved = eventRepository.save(event);
        return EventMapper.toEventFullDto(saved);
    }

    public EventFullDto getEventOfUser(Long userId, Long eventId) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю id=" + userId);
        }
        return EventMapper.toEventFullDto(event);

    }

    @Transactional
    public EventFullDto updateEventOfUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит пользователю id=" + userId);
        }

        if (EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Нельзя изменять уже опубликованное событие");

        }

        if (dto.getEventDate() != null) {
            checkEventDate(dto.getEventDate());
        }


        Category category = null;
        if (dto.getCategory() != null) {
            category = getCategoryOrThrow(dto.getCategory());
        }
        if (dto.getStateAction() != null) {
            updateState(event, dto.getStateAction());
        }
        EventMapper.updateEventFromUserRequest(event, dto, category);
        Event updated = eventRepository.save(event);

        return EventMapper.toEventFullDto(updated);
    }

    // Вспомогательные методы
    private void updateState(Event event, String stateAction) {
        switch (stateAction) {
            case "CANCEL_REVIEW":
                if (EventState.PENDING.equals(event.getState())) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new ConflictException("Событие можно отменить только в состоянии PENDING.");
                }
                break;
            case "SEND_TO_REVIEW":
                if (EventState.PENDING.equals(event.getState()) || EventState.CANCELED.equals(event.getState())) {
                    event.setState(EventState.PENDING);
                } else {
                    throw new ConflictException("Событие можно отправить на модерацию только в состоянии PENDING.");
                }
                break;
            default:
                throw new ValidationException("Некорректное значение stateAction: " + stateAction);
        }
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
    }

    private User getUserOrThrow(Long userId) {
        return userServiceClient.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private void checkEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new ConflictException(
                    "Дата события не может быть раньше, чем через " + HOURS_BEFORE_EVENT + " часа(ов) от текущего момента."
            );
        }
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestServiceClient.getCountConfirmedRequestsByEventId(eventId);
    }

}
