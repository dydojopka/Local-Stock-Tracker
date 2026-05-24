package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.model.ProductInfo;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductInfoRepository productInfoRepository;

    @GetMapping
    public String showCatalog(Model model) {
        // Передаем весь справочник продуктов на страницу
        model.addAttribute("catalogItems", productInfoRepository.findAll());
        return "catalog";
    }

    @PostMapping("/update-min-quantity")
    public String updateMinQuantity(@RequestParam Integer id, @RequestParam double minQuantity) {
        // Находим продукт, обновляем порог и сохраняем
        ProductInfo info = productInfoRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Продукт не найден: " + id)
        );
        info.setMinQuantity(minQuantity);
        productInfoRepository.save(info);

        return "redirect:/catalog"; // Перезагружаем страницу
    }
}