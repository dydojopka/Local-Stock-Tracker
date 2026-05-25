package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.model.Product;
import org.ashroev.repository.ProductInfoRepository;
import org.ashroev.service.InventoryManager;
import org.springframework.format.annotation.DateTimeFormat; // Важный импорт для работы с датами
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryManager inventoryManager;
    private final ProductInfoRepository productInfoRepository;

    // Отображение формы списания для конкретного продукта
    @GetMapping("/write-off/{id}")
    public String showWriteOffForm(@PathVariable Integer id, Model model) {
        Product product = inventoryManager.getAvailableStock().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Продукт не найден"));

        model.addAttribute("product", product);
        return "write-off"; // Имя HTML-шаблона
    }

    // Обработка отправки формы списания (ТЕПЕРЬ ПОЛНОСТЬЮ СОВПАДАЕТ С ИСТОРИЕЙ)
    @PostMapping("/write-off")
    public String writeOffProduct(
            @RequestParam Integer productId,
            @RequestParam Double amount,
            @RequestParam String reason) {

        try {
            // Передаем причину (USED, EXPIRED, DELETE) в сервис
            inventoryManager.writeOffProduct(productId, amount, reason);
            return "redirect:/products?writeOffSuccess";
        } catch (IllegalArgumentException e) {
            // Если ошибка (например, списали больше, чем есть), возвращаем обратно с сообщением
            return "redirect:/products/write-off/" + productId + "?error";
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
        model.addAttribute("productInfos", productInfoRepository.findAll());
        return "add-product";
    }

    // Обработка отправки формы
    @PostMapping("/add")
    public String addProduct(
            @RequestParam Integer productInfoId,
            @RequestParam Double quantity,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate expiryDate) { // Исправлено здесь

        inventoryManager.addProduct(productInfoId, quantity, expiryDate);

        // После сохранения перенаправляем на главную страницу с флагом успеха
        return "redirect:/products?success";
    }
}