package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.service.InventoryManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final InventoryManager inventoryManager;

    @GetMapping
    public String showNotifications(Model model) {
        // Указываем порог в 3 дня для предупреждений
        model.addAttribute("expiringProducts", inventoryManager.getExpiringProducts(3));
        model.addAttribute("expiredProducts", inventoryManager.getExpiredProducts());
        return "notifications";
    }
}