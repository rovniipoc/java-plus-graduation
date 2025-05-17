package ru.practicum.feign;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users")
public interface RequestServiceClient {

    @GetMapping("/{userId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getRequestsOfUser(@PathVariable Long userId) throws FeignException;

    @PostMapping("/{userId}/requests")
    ResponseEntity<ParticipationRequestDto> addRequest(@PathVariable Long userId, @RequestParam Long eventId) throws FeignException;

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) throws FeignException;

    @GetMapping("/{userId}/requests/{eventId}")
    ResponseEntity<List<ParticipationRequestDto>> getRequestsForUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @PatchMapping("/{userId}/requests/{eventId}/change")
    ResponseEntity<EventRequestStatusUpdateResult> changeRequestsStatus(@PathVariable Long userId,
                                                                        @PathVariable Long eventId,
                                                                        @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) throws FeignException;

    @GetMapping("/{userId}/requests/{eventId}/confirmedcount")
    Long getCountConfirmedRequestsByEventId(@PathVariable Long eventId) throws FeignException;
}
