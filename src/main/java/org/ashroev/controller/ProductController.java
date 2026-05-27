package org.ashroev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ashroev.dto.ProductCreateRequest;
import org.ashroev.dto.WriteOffRequest;
import org.ashroev.model.Product;
import org.ashroev.repository.ProductInfoRepository;
import org.ashroev.service.InventoryManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryManager inventoryManager;
    private final ProductInfoRepository productInfoRepository;

    // Форма списания
    @GetMapping("/write-off/{id}")
    public String showWriteOffForm(@PathVariable Integer id, Model model) {
        Product product = inventoryManager.getAvailableProduct(id);
        WriteOffRequest writeOffForm = new WriteOffRequest();
        writeOffForm.setProductId(id);

        model.addAttribute("product", product);
        model.addAttribute("writeOffForm", writeOffForm);
        return "write-off"; // Имя HTML-шаблона
    }

    // Списание товара
    @PostMapping("/write-off")
    public String writeOffProduct(
            @Valid @ModelAttribute("writeOffForm") WriteOffRequest writeOffForm,
            BindingResult bindingResult,
            Model model) {

        if (writeOffForm.getProductId() == null) {
            throw new IllegalArgumentException("Не выбран продукт для списания");
        }

        Product product = inventoryManager.getAvailableProduct(writeOffForm.getProductId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("product", product);
            return "write-off";
        }

        try {
            // Передаем причину (USED, EXPIRED, DELETE) в сервис
            inventoryManager.writeOffProduct(
                    writeOffForm.getProductId(),
                    writeOffForm.getAmount(),
                    writeOffForm.getReason()
            );
            return "redirect:/products?writeOffSuccess";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("writeOffError", e.getMessage());
            model.addAttribute("product", product);
            return "write-off";
        }
    }

    // Главная страница: отображает таблицу всех запасов
    @GetMapping
    public String showStockList(Model model) {
        List<Product> stock = inventoryManager.getAvailableStock();
        model.addAttribute("products", stock);
        return "stock-list"; // Имя HTML-файла
    }

    // Отображение формы добавления товара
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", new ProductCreateRequest());
        }
        model.addAttribute("productInfos", productInfoRepository.findAll());
        return "add-product";
    }

    // Обработка отправки формы
    @PostMapping("/add")
    public String addProduct(
            @Valid @ModelAttribute("productForm") ProductCreateRequest productForm,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productInfos", productInfoRepository.findAll());
            return "add-product";
        }

        inventoryManager.addProduct(
                productForm.getProductInfoId(),
                productForm.getQuantity(),
                productForm.getExpiryDate()
        );

        // После сохранения перенаправляем на главную страницу с флагом успеха
        return "redirect:/products?success";
    }
}
