package ru.practicum.feign;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.model.Event;

import java.util.Optional;
import java.util.Set;

@FeignClient(name = "event-service", contextId = "eventServiceClient")
public interface EventServiceClient {

    @GetMapping("/admin/events/existsbycategory/{id}")
    boolean existsByCategoryId(@PathVariable Long id) throws FeignException;

    @GetMapping("/events/{id}/full")
    Optional<Event> getEventFullById(@PathVariable long id) throws FeignException;

    @PostMapping("/admin/events")
    Event saveEvent(@RequestBody Event event) throws FeignException;

    @GetMapping("/admin/events/findbyidin")
    Set<Event> findByIdIn(@RequestParam Set<Long> ids) throws FeignException;
}
