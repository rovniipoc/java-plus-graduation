package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.AdminEventService;
import ru.practicum.validation.UpdateGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Validated
public class AdminEventController {

    private final AdminEventService adminEventService;

    @PatchMapping("/{id}")
    public EventFullDto updateEvent(@Validated(UpdateGroup.class) @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                    @PathVariable Long id) {
        log.info("Поступил запрос Patch /admin/events/{} на обновление Event с телом = {}", id, updateEventAdminRequest);
        EventFullDto response = adminEventService.updateEvent(id, updateEventAdminRequest);
        log.info("Сформирован ответ Patch /admin/events/{} с телом: {}", id, response);
        return response;
    }

    @PostMapping
    public Event saveEvent(@RequestBody Event event) {
        log.info("Поступил запрос Post /admin/events на сохранение Event с телом = {}", event);
        Event response = adminEventService.saveEventFull(event);
        log.info("Сформирован ответ Post /admin/events с телом: {}", response);
        return response;
    }

    @GetMapping
    public List<EventFullDto> getEventsByParams(@RequestParam(required = false) List<Long> userIds,
                                                @RequestParam(required = false) List<EventState> states,
                                                @RequestParam(required = false) List<Long> categoryIds,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(required = false, defaultValue = "0") Long from,
                                                @RequestParam(required = false, defaultValue = "10") Long size) {
        log.info("Поступил запрос Get /admin/events на получение Events с параметрами: userIds = {}, " +
                 "states = {}, categoryIds = {}, rangeStart = {}, rangeEnd = {}, from = {}, size = {}",
                userIds, states, categoryIds, rangeStart, rangeEnd, from, size);
        List<EventFullDto> response = adminEventService.findEventByParams(userIds, states, categoryIds, rangeStart, rangeEnd, from, size);
        log.info("Сформирован ответ Get /admin/events с телом: {}", response);
        return response;
    }

    @GetMapping("/existsbycategory/{id}")
    public boolean existsByCategoryId(@PathVariable Long id) {
        log.info("Поступил запрос Get /admin/events/existsbycategory/{} на проверку существования события по id категории = {}", id, id);
        boolean response = adminEventService.existsByCategoryId(id);
        log.info("Сформирован ответ Get /admin/events/existsbycategory/{} с телом: {}", id, response);
        return response;
    }

    @GetMapping("/findbyidin")
    public Set<Event> findByIdIn(@RequestParam(required = false) Set<Long> ids) {
        log.info("Поступил запрос Get /admin/events/findbyidin на получение событий по списку id {}", ids);
        Set<Event> response = adminEventService.findByIdIn(ids);
        log.info("Сформирован ответ Get /admin/events/findbyidin с телом: {}", response);
        return response;
    }
}
