package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    CompilationDto getCompilationById(long id);

    List<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);
}
