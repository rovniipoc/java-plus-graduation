package ru.practicum.feign;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", path = "/admin/events")
public interface EventServiceClient {

    @GetMapping("/existsbycategory/{id}")
    boolean existsByCategoryId(@PathVariable Long id) throws FeignException;
}
