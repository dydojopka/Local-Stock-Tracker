package org.ashroev.repository;

import org.ashroev.model.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {
    // Методы для будущих графиков (выборка за неделю/месяц) мы напишем здесь позже
}