package org.ashroev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ashroev.dto.ProductInfoCreateRequest;
import org.ashroev.exception.ResourceNotFoundException;
import org.ashroev.model.Category;
import org.ashroev.model.ProductInfo;
import org.ashroev.repository.CategoryRepository;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/directory")
@RequiredArgsConstructor
public class ProductInfoController {

    private final ProductInfoRepository productInfoRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String listProductInfos(Model model) {
        if (!model.containsAttribute("productInfoForm")) {
            model.addAttribute("productInfoForm", new ProductInfoCreateRequest());
        }
        populateDirectoryModel(model);
        return "product-infos";
    }

    @PostMapping("/add")
    public String addProductInfo(
            @Valid @ModelAttribute("productInfoForm") ProductInfoCreateRequest productInfoForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            populateDirectoryModel(model);
            return "product-infos";
        }

        String barcode = StringUtils.hasText(productInfoForm.getBarcode())
                ? productInfoForm.getBarcode().trim()
                : null;

        try {
            Category category = categoryRepository.findById(productInfoForm.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

            ProductInfo info = new ProductInfo();
            info.setName(productInfoForm.getName().trim());
            info.setBarcode(barcode);
            info.setMinQuantity(productInfoForm.getMinQuantity());
            info.setCategory(category);

            productInfoRepository.save(info);
            return "redirect:/directory?success";

        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("barcode", "barcode.duplicate", "Продукт с таким штрих-кодом уже зарегистрирован");
            populateDirectoryModel(model);
            return "product-infos";
        }
    }

    @PostMapping("/delete")
    public String deleteProductInfo(@RequestParam Integer id) {
        productInfoRepository.deleteById(id);
        return "redirect:/directory";
    }

    private void populateDirectoryModel(Model model) {
        model.addAttribute("productInfos", productInfoRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
    }
}
