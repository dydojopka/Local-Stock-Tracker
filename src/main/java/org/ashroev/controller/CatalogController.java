package org.ashroev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ashroev.dto.ProductMinQuantityRequest;
import org.ashroev.exception.ResourceNotFoundException;
import org.ashroev.model.ProductInfo;
import org.ashroev.repository.ProductInfoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String updateMinQuantity(
            @Valid @ModelAttribute ProductMinQuantityRequest minQuantityRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", getFirstErrorMessage(bindingResult));
            return "redirect:/catalog";
        }

        // Находим продукт, обновляем порог и сохраняем
        ProductInfo info = productInfoRepository.findById(minQuantityRequest.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Продукт не найден: " + minQuantityRequest.getId())
        );
        info.setMinQuantity(minQuantityRequest.getMinQuantity());
        productInfoRepository.save(info);

        return "redirect:/catalog";
    }

    private String getFirstErrorMessage(BindingResult bindingResult) {
        ObjectError error = bindingResult.getAllErrors().get(0);
        return error.getDefaultMessage();
    }
}
