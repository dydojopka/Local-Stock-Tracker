package org.ashroev.service;

import org.ashroev.dto.ChartDataResponse;
import org.ashroev.dto.DashboardStatsResponse;
import org.ashroev.dto.DisposalChartResponse;
import org.ashroev.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InventoryManager {

    private final ProductRepository productRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;

    private final ProductHistoryRepository productHistoryRepository;

    /**
     * Добавление партии товара
     */
    public Product addProduct(Integer productInfoId, Double quantity, LocalDate expiryDate) {
        // Ищем описание продукта по ID
        ProductInfo info = productInfoRepository.findById(productInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("Описание продукта не найдено"));

        // Создаем партию
        Product product = new Product();
        product.setProductInfo(info);
        product.setQuantity(quantity);
        product.setExpiryDate(expiryDate);
        product.setAddedDate(LocalDate.now());

        return productRepository.save(product);
    }

    /**
     * Проверка сроков годности
     */
    public List<Product> checkStock() {
        // Находим все продукты, срок годности которых истекает сегодня или уже истек
        return productRepository.findByExpiryDateBefore(LocalDate.now().plusDays(1));
    }

    /**
     * Текущие остатки
     */
    public List<Product> getAvailableStock() {
        return productRepository.findByQuantityGreaterThan(0.0);
    }

    /**
     * Получение партии продукта
     */
    public Product getAvailableProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден"));

        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            throw new ResourceNotFoundException("Продукт не найден среди текущих остатков");
        }

        return product;
    }

    /**
     * Списание товара
     */
    @Transactional
    public void writeOffProduct(Integer productId, Double amount, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден"));

        // Защита от ошибок Double
        Double currentQty = Math.round(product.getQuantity() * 1000.0) / 1000.0;
        Double amountToOff = Math.round(amount * 1000.0) / 1000.0;

        if (amountToOff <= 0) {
            throw new IllegalArgumentException("Количество для списания должно быть больше нуля");
        }

        if (amountToOff > currentQty) {
            throw new IllegalArgumentException("Количество для списания превышает текущий остаток!");
        }

        if (!"DELETE".equalsIgnoreCase(reason)) {
            WriteOffReason writeOffReason;
            if ("USED".equalsIgnoreCase(reason)) {
                writeOffReason = WriteOffReason.USED;
            } else if ("EXPIRED".equalsIgnoreCase(reason)) {
                writeOffReason = WriteOffReason.EXPIRED;
            } else {
                throw new IllegalArgumentException("Неверная причина списания: " + reason);
            }

            ProductHistory logEntry = new ProductHistory(
                    product.getProductInfo(),
                    amountToOff,
                    writeOffReason,
                    LocalDateTime.now()
            );
            productHistoryRepository.save(logEntry);
        }

        Double newQuantity = currentQty - amountToOff;

        // Считаем остаток нулевым
        if (newQuantity < 0.001) {
            newQuantity = 0.0;
        }

        product.setQuantity(newQuantity);
        productRepository.save(product);
    }

    /**
     * Генерация списка покупок
     */
    public void generateShoppingList() {
        // Получаем справочник всех продуктов
        List<ProductInfo> allProducts = productInfoRepository.findAll();
        List<ShoppingListItem> itemsToBuy = new ArrayList<>();

        for (ProductInfo info : allProducts) {
            // Считаем остаток, ИГНОРИРУЯ просроченные продукты
            LocalDate today = LocalDate.now();
            Double currentTotal = productRepository.findByProductInfo(info).stream()
                    // Оставляем только те партии, где срок годности еще не истек
                    .filter(p -> p.getExpiryDate() == null || !p.getExpiryDate().isBefore(today))
                    .mapToDouble(Product::getQuantity)
                    .sum();

            // Проверка минимального остатка
            if (currentTotal < info.getMinQuantity()) {
                // Вычисляем, сколько нужно докупить
                Double deficit = info.getMinQuantity() - currentTotal;

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

        // Создаем список покупок
        ShoppingList list = new ShoppingList();
        list.setCreatedDate(LocalDate.now());
        list.setStatus("Активен");

        // Привязка элементов
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
        // База данных сама отфильтрует продукты по дате и количеству (> 0)
        return productRepository.findByExpiryDateBetweenAndQuantityGreaterThan(today, thresholdDate, 0.0);
    }

    /**
     * Получить список уже просроченных продуктов, которые всё ещё числятся на складе
     */
    public List<Product> getExpiredProducts() {
        // База данных вернет только просрочку, которая физически есть на складе
        return productRepository.findByExpiryDateBeforeAndQuantityGreaterThan(LocalDate.now(), 0.0);
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

        // Получаем только активные партии из бд
        List<Product> activeProducts = productRepository.findByQuantityGreaterThan(0.0);

        // --- МЕТРИКИ ВСЕГО НА СКЛАДЕ ---
        long totalBatches = activeProducts.size();
        // Считаем сумму всех штук/кг во всех активных партиях
        Double totalQuantity = activeProducts.stream()
                .mapToDouble(Product::getQuantity)
                .sum();

        stats.put("totalProducts", totalBatches); // Оставляем для совместимости (кол-во партий)
        stats.put("totalQuantity", totalQuantity); // Честное кол-во товара на складе

        // --- МЕТРИКИ ПРОСРОЧКИ ---
        List<Product> expiredProducts = activeProducts.stream()
                .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(java.time.LocalDate.now()))
                .collect(Collectors.toList());

        long expiredBatchesCount = expiredProducts.size();
        Double expiredQuantitySum = expiredProducts.stream()
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
                    Double inStock = currentStock.getOrDefault(info, 0.0);
                    return inStock < info.getMinQuantity();
                })
                .count();
        stats.put("lowStockCount", lowStockCount);

        // Процент испорченного товара от общего объема
        int healthScore = 100;
        if (totalQuantity > 0) {
            Double expiredPercent = (expiredQuantitySum / totalQuantity) * 100;
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
                        LinkedHashMap::new,
                        Collectors.summingDouble(Product::getQuantity) // <-- Теперь график покажет реальный объем еды!
                ));

        stats.put("categoryLabels", new ArrayList<>(categoryDistribution.keySet()));
        stats.put("categoryData", new ArrayList<>(categoryDistribution.values()));

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

    @SuppressWarnings("unchecked")
    public DashboardStatsResponse getDashboardStatsResponse() {
        Map<String, Object> stats = getDashboardStats();

        return new DashboardStatsResponse(
                ((Number) stats.get("totalProducts")).longValue(),
                ((Number) stats.get("totalQuantity")).doubleValue(),
                ((Number) stats.get("expiredCount")).longValue(),
                ((Number) stats.get("expiredQuantity")).doubleValue(),
                ((Number) stats.get("activeListsCount")).longValue(),
                ((Number) stats.get("lowStockCount")).longValue(),
                ((Number) stats.get("healthScore")).intValue(),
                new ChartDataResponse<>(
                        (List<String>) stats.get("categoryLabels"),
                        (List<Double>) stats.get("categoryData")
                ),
                new ChartDataResponse<>(
                        (List<String>) stats.get("expiryLabels"),
                        (List<Long>) stats.get("expiryData")
                ),
                new DisposalChartResponse(
                        (List<String>) stats.get("lineChartLabels"),
                        (List<Long>) stats.get("consumedData"),
                        (List<Long>) stats.get("expiredData")
                )
        );
    }
}
