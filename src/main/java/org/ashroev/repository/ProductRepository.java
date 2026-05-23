package org.ashroev.repository;

import org.ashroev.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    // Найти все продукты, у которых срок годности истекает до указанной даты
    List<Product> findByExpiryDateBefore(LocalDate date);

    // Найти все продукты, количество которых больше нуля
    List<Product> findByQuantityGreaterThan(Double quantity);
}