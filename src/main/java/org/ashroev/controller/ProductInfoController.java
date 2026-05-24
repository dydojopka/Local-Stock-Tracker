package org.ashroev.controller;

import org.ashroev.model.Category;
import org.ashroev.model.ProductInfo;
import org.ashroev.repository.CategoryRepository;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/directory") // путь в браузере будет /directory
public class ProductInfoController {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Отображение списка справочника
    @GetMapping
    public String listProductInfos(Model model) {
        model.addAttribute("productInfos", productInfoRepository.findAll());
        // Передаем список категорий для выпадающего списка в форме добавления
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-infos";
    }

    // Добавление новой записи в справочник
    @PostMapping("/add")
    public String addProductInfo(@RequestParam String name,
                                 @RequestParam Integer categoryId) {
        ProductInfo info = new ProductInfo();
        info.setName(name);

        // Находим категорию по ID и привязываем к продукту
        Category category = categoryRepository.findById(categoryId).orElse(null);
        info.setCategory(category);

        productInfoRepository.save(info);
        return "redirect:/directory";
    }

    // Удаление записи из справочника
    @PostMapping("/delete")
    public String deleteProductInfo(@RequestParam Integer id) {
        productInfoRepository.deleteById(id);
        return "redirect:/directory";
    }
}