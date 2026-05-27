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
        Map<String, Object> stats = inventoryManager.getDashboardStats();

        model.addAllAttributes(stats);
        model.addAttribute("activePage", "home");

        return "home";
    }
}
