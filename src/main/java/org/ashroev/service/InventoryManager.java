package org.ashroev.service;

import org.ashroev.repository.ShoppingListItemRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InventoryManager {

    // Подключаем наши репозитории для работы с БД
    private final ProductRepository productRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    private final ProductHistoryRepository productHistoryRepository;

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

    @Transactional
    public void toggleShoppingListItem(Integer itemId, boolean isPurchased) {
        // Находим позицию в списке
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Элемент не найден"));

        // Обновляем статус
        item.setIsPurchased(isPurchased);
        shoppingListItemRepository.save(item);

        // Проверяем, куплены ли все товары в этом списке
        ShoppingList list = item.getShoppingList();
        boolean allPurchased = list.getItems().stream().allMatch(ShoppingListItem::getIsPurchased);

        // Автоматически меняем статус всего списка
        if (allPurchased) {
            list.setStatus("Выполнен");
        } else {
            list.setStatus("Активен");
        }
        shoppingListRepository.save(list);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Получаем только активные партии (где остаток > 0)
        List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(Collectors.toList());

        // --- МЕТРИКИ ВСЕГО НА СКЛАДЕ ---
        long totalBatches = activeProducts.size();
        // Считаем сумму всех штук/кг во всех активных партиях
        double totalQuantity = activeProducts.stream()
                .mapToDouble(Product::getQuantity)
                .sum();

        stats.put("totalProducts", totalBatches); // Оставляем для совместимости (кол-во партий)
        stats.put("totalQuantity", totalQuantity); // Честное кол-во товара на складе

        // --- МЕТРИКИ ПРОСРОЧКИ ---
        List<Product> expiredProducts = activeProducts.stream()
                .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(java.time.LocalDate.now()))
                .collect(Collectors.toList());

        long expiredBatchesCount = expiredProducts.size();
        double expiredQuantitySum = expiredProducts.stream()
                .mapToDouble(Product::getQuantity)
                .sum();

        stats.put("expiredCount", expiredBatchesCount); // Сколько партий выкинуть
        stats.put("expiredQuantity", expiredQuantitySum); // Сколько именно ШТУК/КГ испортилось

        // --- АКТИВНЫЕ СПИСКИ ПОКУПОК ---
        long activeListsCount = shoppingListRepository.countByStatus("Активен");
        stats.put("activeListsCount", activeListsCount);

        // --- ТОВАРЫ, КОТОРЫЕ ЗАКАНЧИВАЮТСЯ (Тут логика уже была правильной, по объемам!) ---
        Map<ProductInfo, Double> currentStock = new HashMap<>();
        activeProducts.stream()
                .filter(p -> p.getExpiryDate() == null || p.getExpiryDate().isAfter(java.time.LocalDate.now()))
                .forEach(p -> currentStock.merge(p.getProductInfo(), p.getQuantity(), Double::sum));

        long lowStockCount = productInfoRepository.findAll().stream()
                .filter(info -> {
                    double inStock = currentStock.getOrDefault(info, 0.0);
                    return inStock < info.getMinQuantity();
                })
                .count();
        stats.put("lowStockCount", lowStockCount);

        // Процент испорченного товара от общего объема
        int healthScore = 100;
        if (totalQuantity > 0) {
            double expiredPercent = (expiredQuantitySum / totalQuantity) * 100;
            // К примеру, каждые 5% просроченного объема снижают здоровье на 10 пунктов,
            // а каждая позиция с низким запасом (lowStock) — на 5 пунктов.
            healthScore = 100 - (int) (expiredPercent * 2) - (int) (lowStockCount * 5);
            if (healthScore < 0) healthScore = 0;
        }
        stats.put("healthScore", healthScore);

        // --- ДАННЫЕ ДЛЯ ГРАФИКА (Группировка по объемам штук/кг, а не по штукам партий!) ---
        Map<String, Double> categoryDistribution = activeProducts.stream()
                .filter(p -> p.getProductInfo() != null && p.getProductInfo().getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getProductInfo().getCategory().getName(),
                        Collectors.summingDouble(Product::getQuantity) // <-- Теперь график покажет реальный объем еды!
                ));

        stats.put("categoryLabels", categoryDistribution.keySet());
        stats.put("categoryData", categoryDistribution.values());

        // Топ-5 продуктов с ближайшим сроком годности
        List<Product> topExpiring = activeProducts.stream()
                .filter(p -> p.getExpiryDate() != null)
                // Оставляем только те, что ЕЩЕ НЕ просрочены (сегодня или в будущем)
                .filter(p -> !p.getExpiryDate().isBefore(java.time.LocalDate.now()))
                // Сортируем по дате по возрастанию (кто испортится быстрее)
                .sorted(Comparator.comparing(Product::getExpiryDate))
                // Берем только первые 5 штук
                .limit(5)
                .collect(Collectors.toList());

        // Вытаскиваем их названия
        List<String> expiryLabels = topExpiring.stream()
                .map(p -> p.getProductInfo().getName())
                .collect(Collectors.toList());

        // Высчитываем разницу в днях между сегодня и сроком годности
        List<Long> expiryData = topExpiring.stream()
                .map(p -> ChronoUnit.DAYS.between(java.time.LocalDate.now(), p.getExpiryDate()))
                .collect(Collectors.toList());

        // Передаем в модель
        stats.put("expiryLabels", expiryLabels);
        stats.put("expiryData", expiryData);

        // --- Данные для графика списания за последние 7 дней ---
        LocalDate today = LocalDate.now();
        List<String> lineChartLabels = new ArrayList<>();
        List<Long> consumedData = new ArrayList<>();
        List<Long> expiredData = new ArrayList<>();
        DateTimeFormatter chartFormatter = DateTimeFormatter.ofPattern("dd.MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            lineChartLabels.add(date.format(chartFormatter));

            // Задаем границы дня (от 00:00:00 до 23:59:59), так как в истории хранится LocalDateTime
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            // Считаем использованные за этот день
            long consumedCount = productHistoryRepository.countByReasonAndActionDateBetween(WriteOffReason.USED, startOfDay, endOfDay);
            long expiredCount = productHistoryRepository.countByReasonAndActionDateBetween(WriteOffReason.EXPIRED, startOfDay, endOfDay);

            consumedData.add(consumedCount);
            expiredData.add(expiredCount);
        }

        // Передаем списки в модель отображения
        stats.put("lineChartLabels", lineChartLabels);
        stats.put("consumedData", consumedData);
        stats.put("expiredData", expiredData);

        return stats;
    }
}