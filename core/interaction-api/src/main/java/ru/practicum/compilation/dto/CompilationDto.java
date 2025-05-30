package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.Set;

@Data
public class CompilationDto {

    private Long id;

    private String title;

    private Boolean pinned;

    private Set<EventShortDto> events;

}
