package org.ashroev.service;

import lombok.RequiredArgsConstructor;
import org.ashroev.model.Product;
import org.ashroev.model.ProductHistory;
import org.ashroev.model.ProductInfo;
import org.ashroev.model.ShoppingList;
import org.ashroev.model.ShoppingListItem;
import org.ashroev.model.WriteOffReason;
import org.ashroev.repository.ProductInfoRepository;
import org.ashroev.repository.ProductRepository;
import org.ashroev.repository.ShoppingListRepository;
import org.ashroev.repository.ProductHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryManager {

    // Подключаем наши репозитории для работы с БД
    private final ProductRepository productRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ShoppingListRepository shoppingListRepository;

    private ProductHistoryRepository productHistoryRepository;

    @Transactional
    public void writeOffProduct(Integer productId, double quantityToRemove, String actionType) {
        // Находим партию продукта на складе
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Продукт на складе не найден с ID: " + productId));

        // Нельзя списать больше, чем есть на складе
        if (quantityToRemove > product.getQuantity()) {
            throw new IllegalArgumentException("Нельзя списать больше, чем есть в наличии");
        }

        // Логика сохранения в историю (только если это НЕ простое удаление ошибки)
        if (!"DELETE".equalsIgnoreCase(actionType)) {
            WriteOffReason reason;
            if ("USED".equalsIgnoreCase(actionType)) {
                reason = WriteOffReason.USED;
            } else if ("EXPIRED".equalsIgnoreCase(actionType)) {
                reason = WriteOffReason.EXPIRED;
            } else {
                throw new IllegalArgumentException("Неверная причина списания: " + actionType);
            }

            // Создаем запись в историю
            ProductHistory logEntry = new ProductHistory(
                    product.getProductInfo(),
                    quantityToRemove,
                    reason,
                    LocalDateTime.now()
            );
            productHistoryRepository.save(logEntry);
        }

        // Изменяем остаток на складе или удаляем запись, если списали всё подчистую
        if (quantityToRemove == product.getQuantity()) {
            productRepository.delete(product);
        } else {
            product.setQuantity(product.getQuantity() - quantityToRemove);
            productRepository.save(product);
        }
    }

    /**
     * Инициация процесса постановки новой партии товара на учет.
     * Создает связь между ProductInfo и новым Product[cite: 88].
     */
    public Product addProduct(Integer productInfoId, Double quantity, LocalDate expiryDate) {
        // Ищем описание продукта по ID
        ProductInfo info = productInfoRepository.findById(productInfoId)
                .orElseThrow(() -> new RuntimeException("Описание продукта не найдено"));

        // Создаем физическую партию
        Product product = new Product();
        product.setProductInfo(info);
        product.setQuantity(quantity);
        product.setExpiryDate(expiryDate);
        product.setAddedDate(LocalDate.now());

        return productRepository.save(product);
    }

    /**
     * Аудит текущих остатков и проверка сроков годности
     */
    public List<Product> checkStock() {
        // Находим все продукты, срок годности которых истекает сегодня или уже истек
        return productRepository.findByExpiryDateBefore(LocalDate.now().plusDays(1));
    }

    /**
     * Получить список всех продуктов, которые физически есть на складе
     */
    public List<Product> getAvailableStock() {
        return productRepository.findByQuantityGreaterThan(0.0);
    }

    /**
     * Списание продукта из запаса (UC-02)
     */
    public void writeOffProduct(Integer productId, Double amount, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Продукт не найден"));

        // Округляем текущее количество и списываемое до 3 знаков после запятой,
        // чтобы избежать проблемы 0.8000000000000001
        double currentQty = Math.round(product.getQuantity() * 1000.0) / 1000.0;
        double amountToOff = Math.round(amount * 1000.0) / 1000.0;

        if (amountToOff > currentQty) {
            throw new IllegalArgumentException("Количество для списания превышает текущий остаток!");
        }

        double newQuantity = currentQty - amountToOff;

        // Если остаток меньше одной тысячной (те самые E-16), считаем, что продукт кончился
        if (newQuantity < 0.001) {
            newQuantity = 0.0;
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);
    }

    /**
     * Алгоритм автоматического формирования списка покупок на основе сравнения
     * текущего количества товара с атрибутом minQuantity в классе ProductInfo
     */
    public void generateShoppingList() {
        // Получаем справочник всех продуктов
        List<ProductInfo> allProducts = productInfoRepository.findAll();
        List<ShoppingListItem> itemsToBuy = new ArrayList<>();

        for (ProductInfo info : allProducts) {
            // Считаем суммарный текущий остаток, ИГНОРИРУЯ просроченные продукты
            LocalDate today = LocalDate.now();
            double currentTotal = productRepository.findByProductInfo(info).stream()
                    // Оставляем только те партии, где срок годности еще не истек
                    .filter(p -> p.getExpiryDate() == null || !p.getExpiryDate().isBefore(today))
                    .mapToDouble(Product::getQuantity)
                    .sum();

            // Сравниваем с заданным минимальным порогом
            if (currentTotal < info.getMinQuantity()) {
                // Вычисляем, сколько нужно докупить
                double deficit = info.getMinQuantity() - currentTotal;

                ShoppingListItem item = new ShoppingListItem();
                item.setProductInfo(info);
                // Округляем
                item.setTargetQuantity(Math.round(deficit * 1000.0) / 1000.0);
                item.setIsPurchased(false);

                itemsToBuy.add(item);
            }
        }

        // Если ни один продукт не опустился ниже порога
        if (itemsToBuy.isEmpty()) {
            throw new IllegalStateException("Запасы в норме, генерация не требуется");
        }

        // Создаем документ "Список покупок" и сохраняем всё в БД
        ShoppingList list = new ShoppingList();
        list.setCreatedDate(LocalDate.now());
        list.setStatus("Активен");

        // Устанавливаем связь строк с самим списком
        for (ShoppingListItem item : itemsToBuy) {
            item.setShoppingList(list);
        }
        list.setItems(itemsToBuy);

        shoppingListRepository.save(list);
    }

    /**
     * Получить список продуктов, срок годности которых истекает в ближайшие N дней
     */
    public List<Product> getExpiringProducts(int daysThreshold) {
        LocalDate today = LocalDate.now();
        LocalDate thresholdDate = today.plusDays(daysThreshold);
        // Ищем продукты, у которых срок годности от сегодня до "сегодня + N дней"
        return productRepository.findByExpiryDateBetween(today, thresholdDate).stream()
                .filter(p -> p.getQuantity() > 0) // Игнорируем те, что уже списаны
                .collect(Collectors.toList());
    }

    /**
     * Получить список уже просроченных продуктов, которые всё ещё числятся на складе
     */
    public List<Product> getExpiredProducts() {
        return productRepository.findByExpiryDateBefore(LocalDate.now()).stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(Collectors.toList());
    }
}