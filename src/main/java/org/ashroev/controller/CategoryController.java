package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.model.Category;
import org.ashroev.repository.CategoryRepository;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ProductInfoRepository productInfoRepository;

    // Отображение списка категорий
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "categories";
    }

    // Добавление новой категории
    @PostMapping("/add")
    public String addCategory(@RequestParam String name, @RequestParam(required = false) String description) {
        try {
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            categoryRepository.save(category);
            return "redirect:/categories";
        } catch (Exception e) {
            return "redirect:/categories?error";
        }
    }

    // Удаление категории
    @PostMapping("/delete")
    public String deleteCategory(@RequestParam Integer id) {
        if (!categoryRepository.existsById(id)) {
            return "redirect:/categories?deleteError=not_found";
        }

        if (productInfoRepository.existsByCategoryId(id)) {
            return "redirect:/categories?deleteError=in_use";
        }

        try {
            categoryRepository.deleteById(id);
            return "redirect:/categories?deleted";
        } catch (DataIntegrityViolationException e) {
            return "redirect:/categories?deleteError=in_use";
        }
    }
}
