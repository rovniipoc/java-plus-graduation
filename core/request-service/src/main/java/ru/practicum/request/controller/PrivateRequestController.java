package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsOfUser(@PathVariable Long userId) {
        log.info("[GET] Запросы пользователя с ID {}", userId);
        List<ParticipationRequestDto> requests = requestService.getRequestsOfUser(userId);

        return ResponseEntity.ok(requests);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addRequest(@PathVariable Long userId,
                                                              @RequestParam Long eventId) {
        log.info("[POST] Добавление запроса на участие. Пользователь: {}, Событие: {}", userId, eventId);
        ParticipationRequestDto request = requestService.addRequest(userId, eventId);

        return ResponseEntity.status(201).body(request);
    }


    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId,
                                                                 @PathVariable Long requestId) {
        log.info("[PATCH] Отмена запроса с ID {} для пользователя {}", userId, requestId);
        ParticipationRequestDto canceledRequest = requestService.cancelRequest(userId, requestId);
        return ResponseEntity.ok(canceledRequest);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<ParticipationRequestDto>> getRequestsForUserEvent(@PathVariable Long userId,
                                                                                 @PathVariable Long eventId) {
        log.info("[GET] Запросы пользователя с ID {} по событию с ID {}", userId, eventId);
        List<ParticipationRequestDto> requests = requestService.getRequestsForUserEvent(userId, eventId);

        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/{eventId}/change")
    public ResponseEntity<EventRequestStatusUpdateResult> changeRequestsStatus(@PathVariable Long userId,
                                                                               @PathVariable Long eventId,
                                                                               @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("[PATCH] Изменение статуса запроса пользователя с ID {} для события с ID {} с телом {}", userId, eventId, statusUpdateRequest);
        EventRequestStatusUpdateResult response = requestService.changeRequestsStatus(userId, eventId, statusUpdateRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/confirmedcount")
    public Long getCountConfirmedRequestsByEventId(@PathVariable Long userId,
                                                   @PathVariable Long eventId) {
        log.info("[GET] Запрос кол-ва подтвержденных заявок по событию с ID {}", eventId);
        Long response = requestService.getConfirmedRequests(userId, eventId);

        return response;
    }

}
