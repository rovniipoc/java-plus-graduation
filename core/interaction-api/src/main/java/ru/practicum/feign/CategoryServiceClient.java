package ru.practicum.feign;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.model.Category;

import java.util.Optional;

@FeignClient(name = "category-service", path = "/categories", contextId = "categoryServiceClient")
public interface CategoryServiceClient {

    @GetMapping("/{catId}/full")
    Optional<Category> getFullCategoriesById(@PathVariable long catId) throws FeignException;

}
