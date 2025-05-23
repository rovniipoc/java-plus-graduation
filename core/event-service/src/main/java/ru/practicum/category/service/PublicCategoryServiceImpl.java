package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryServiceImpl implements PublicCategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        List<Category> categories = categoryRepository.findAll(PageRequest.of(from, size)).toList();

        List<CategoryDto> categoriesDto = categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
        log.info("получен список categoryDtoResp from = " + from + " size " + size);
        return categoriesDto;
    }

    @Override
    public CategoryDto getCategoryById(long id) {
        CategoryDto categoryDto = CategoryMapper
                .mapToCategoryDto(categoryRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Категория " + id + "не найдена")));
        log.info("получен categoryDtoResp с ID = {}", categoryDto.getId());
        return categoryDto;
    }
}
