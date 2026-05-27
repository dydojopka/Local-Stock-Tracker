package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.ashroev.repository.ShoppingListRepository;
import org.ashroev.service.InventoryManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/shopping")
@RequiredArgsConstructor
public class ShoppingListController {

    private final InventoryManager inventoryManager;
    private final ShoppingListRepository shoppingListRepository;

    // Отображение раздела "Списки покупок"
    @GetMapping
    public String showLists(Model model) {
        model.addAttribute("shoppingLists", shoppingListRepository.findAll());
        return "shopping-lists"; // Имя HTML шаблона
    }

    // Генерация списка
    @PostMapping("/generate")
    public String generateList() {
        try {
            inventoryManager.generateShoppingList();
            return "redirect:/shopping?success";
        } catch (IllegalStateException e) {
            // Список не требуется
            return "redirect:/shopping?notNeeded";
        }
    }

    // Удаление списка
    @PostMapping("/delete/{id}")
    public String deleteList(@PathVariable Integer id) {
        shoppingListRepository.deleteById(id);
        return "redirect:/shopping?deleted";
    }

    // Обработка нажатия на чекбокс (AJAX)
    @PostMapping("/toggle-item/{itemId}")
    @ResponseBody
    public ResponseEntity<?> toggleItem(@PathVariable Integer itemId, @RequestParam boolean purchased) {
        inventoryManager.toggleShoppingListItem(itemId, purchased);
        return ResponseEntity.ok().build();
    }
}