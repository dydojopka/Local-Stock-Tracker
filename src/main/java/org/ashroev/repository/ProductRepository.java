package org.ashroev.repository;

import org.ashroev.model.Product;
import org.ashroev.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByExpiryDateBefore(LocalDate date);
    List<Product> findByQuantityGreaterThan(Double quantity);

    // Поиск всех партий по описанию продукта
    List<Product> findByProductInfo(ProductInfo productInfo);

    boolean existsByProductInfoId(Integer productInfoId);

    // Найти продукты, срок годности которых попадает в заданный диапазон
    List<Product> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);
}
