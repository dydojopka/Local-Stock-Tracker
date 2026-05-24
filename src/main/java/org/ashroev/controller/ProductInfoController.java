package org.ashroev.controller;

import org.ashroev.model.Category;
import org.ashroev.model.ProductInfo;
import org.ashroev.repository.CategoryRepository;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/directory")
public class ProductInfoController {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String listProductInfos(Model model) {
        model.addAttribute("productInfos", productInfoRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "product-infos";
    }

    @PostMapping("/add")
    public String addProductInfo(
            @RequestParam String name,
            @RequestParam(required = false) String barcode,
            @RequestParam Double minQuantity,
            @RequestParam Integer categoryId) {

        if (minQuantity != null && minQuantity < 0) {
            return "redirect:/directory?error=negative";
        }

        if (barcode != null && barcode.trim().isEmpty()) {
            barcode = null;
        }

        try {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

            ProductInfo info = new ProductInfo();
            info.setName(name);
            info.setBarcode(barcode);
            info.setMinQuantity(minQuantity);
            info.setCategory(category);

            productInfoRepository.save(info);
            return "redirect:/directory?success";

        } catch (Exception e) {
            return "redirect:/directory?error=duplicate_barcode";
        }
    }

    @PostMapping("/delete")
    public String deleteProductInfo(@RequestParam Integer id) {
        productInfoRepository.deleteById(id);
        return "redirect:/directory";
    }
}