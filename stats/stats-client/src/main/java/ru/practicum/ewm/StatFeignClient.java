package ru.practicum.ewm;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server", contextId = "statsServiceClient", fallback = Fallback.class)
public interface StatFeignClient {
    @PostMapping("/hit")
    EndpointHitInputDto addHit(@RequestBody EndpointHitInputDto hitDto);

    @GetMapping("/stats")
    ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(required = false) Boolean unique);
}

@Component
class Fallback implements StatFeignClient {

    @Override
    public EndpointHitInputDto addHit(@RequestBody EndpointHitInputDto hitDto) {
        return new EndpointHitInputDto();
    }

    @Override
    public ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false) Boolean unique) {
        List<ViewStatsOutputDto> response = List.of(new ViewStatsOutputDto());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}