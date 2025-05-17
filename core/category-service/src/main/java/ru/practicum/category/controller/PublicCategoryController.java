package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.AdminCategoryService;
import ru.practicum.category.service.PublicCategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/categories")
public class PublicCategoryController {
    private final PublicCategoryService publicCategoryService;
    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        log.info("GET-запрос к эндпоинту: '/categories' на получение categories (from = {}, size = {}", from, size);
        List<CategoryDto> response = publicCategoryService.getAllCategories(from, size);
        log.info("Сформирован ответ Get /categories с телом: {}", response);
        return response;
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoriesById(@PathVariable long catId) {
        log.info("GET-запрос к эндпоинту: '/categories/{}' на получение categories", catId);
        CategoryDto response = publicCategoryService.getCategoryById(catId);
        log.info("Сформирован ответ Get /categories/{} с телом: {}", catId, response);
        return response;
    }

    @GetMapping("/{catId}/full")
    public Optional<Category> getFullCategoriesById(@PathVariable long catId) {
        log.info("GET-запрос к эндпоинту: '/categories/{}/full' на получение categories model", catId);
        Optional<Category> response = adminCategoryService.getFullCategoryById(catId);
        log.info("Сформирован ответ Get /categories/{}/full с телом: {}", catId, response);
        return response;
    }
}
