package org.ashroev.repository;

import org.ashroev.model.ProductHistory;
import org.ashroev.model.WriteOffReason;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {
    // Этот метод посчитает, сколько продуктов было списано по конкретной причине за промежуток времени
    long countByReasonAndTimestampBetween(WriteOffReason reason, LocalDateTime start, LocalDateTime end);
}