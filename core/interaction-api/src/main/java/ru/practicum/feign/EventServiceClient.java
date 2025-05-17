package ru.practicum.feign;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.event.model.Event;

import java.util.Optional;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/admin/events/existsbycategory/{id}")
    boolean existsByCategoryId(@PathVariable Long id) throws FeignException;

    @GetMapping("/events/{id}/full")
    Optional<Event> getEventFullById(@PathVariable long id) throws FeignException;

    @PostMapping("/admin/events")
    Event saveEvent(@RequestBody Event event) throws FeignException;
}
