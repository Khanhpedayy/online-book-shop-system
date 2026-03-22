package com.example.onlinebookshop.controller;

import com.example.onlinebookshop.Entity.Category;
import com.example.onlinebookshop.Repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> getCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getActive() == null || Boolean.TRUE.equals(c.getActive()))
                .sorted(Comparator.comparing(Category::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(c -> c.getName() != null ? c.getName() : "", String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
