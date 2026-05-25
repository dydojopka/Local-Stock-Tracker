package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.service.InventoryManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final InventoryManager inventoryManager;

    @GetMapping("/")
    public String index(Model model) {
        // Получаем просчитанную статистику из сервиса
        Map<String, Object> stats = inventoryManager.getDashboardStats();

        // Передаем переменные в Thymeleaf
        model.addAllAttributes(stats);
        model.addAttribute("activePage", "home"); // Чтобы кнопка в меню подсвечивалась

        return "home"; // Имя файла шаблона (home.html)
    }
}
