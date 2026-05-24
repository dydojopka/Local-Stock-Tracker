package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.repository.ShoppingListRepository;
import org.ashroev.service.InventoryManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    // Обработка нажатия кнопки генерации
    @PostMapping("/generate")
    public String generateList() {
        try {
            inventoryManager.generateShoppingList();
            return "redirect:/shopping?success";
        } catch (IllegalStateException e) {
            // Перехватываем наше сообщение "Запасы в норме" и выводим на экран
            return "redirect:/shopping?notNeeded";
        }
    }

    // Обработка удаления списка
    @PostMapping("/delete/{id}")
    public String deleteList(@PathVariable Integer id) {
        shoppingListRepository.deleteById(id);
        return "redirect:/shopping?deleted";
    }
}