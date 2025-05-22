package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.AdminCompilationService;
import ru.practicum.validation.CreateGroup;
import ru.practicum.validation.UpdateGroup;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {

    private final AdminCompilationService adminCompilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Validated(CreateGroup.class) @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Поступил запрос Post /admin/compilations на создание Compilation с телом {}", newCompilationDto);
        CompilationDto response = adminCompilationService.createCompilation(newCompilationDto);
        log.info("Сформирован ответ Post /admin/compilations с телом: {}", response);
        return response;
    }

    @PatchMapping("/{id}")
    public CompilationDto updateCompilation(@Validated(UpdateGroup.class) @RequestBody UpdateCompilationRequest updateCompilationRequest,
                                            @PathVariable Long id) {
        log.info("Поступил запрос Patch /admin/compilations/{} на обновление Compilation с телом {}", id, updateCompilationRequest);
        CompilationDto response = adminCompilationService.updateCompilation(updateCompilationRequest, id);
        log.info("Сформирован ответ Patch /admin/compilations/{} с телом: {}", id, response);
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long id) {
        log.info("Поступил запрос Delete /admin/compilations/{} на удаление Compilation с id = {}", id, id);
        adminCompilationService.deleteCompilationById(id);
        log.info("Выполнен запрос Delete /admin/compilations/{} на удаление Compilation с id = {}", id, id);
    }

}
