package org.ashroev.controller;

import org.ashroev.model.Category;
import org.ashroev.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

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
        // Здесь можно добавить проверку: если категория используется в продуктах, не удалять (или удалять каскадно)
        // Пока делаем простое удаление для базового функционала
        categoryRepository.deleteById(id);
        return "redirect:/categories";
    }
}