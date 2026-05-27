package org.ashroev.repository;

import org.ashroev.model.ProductHistory;
import org.ashroev.model.WriteOffReason;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {
    // Этот метод посчитает, сколько продуктов было списано по конкретной причине за промежуток времени
    long countByReasonAndActionDateBetween(WriteOffReason reason, LocalDateTime start, LocalDateTime end);

    @Transactional
    @Modifying
    @Query("update ProductHistory history set history.productInfo = null where history.productInfo.id = :productInfoId")
    void detachProductInfo(@Param("productInfoId") Integer productInfoId);

    @Transactional
    @Modifying
    @Query("delete from ProductHistory history where history.productInfo.id = :productInfoId")
    void deleteByProductInfoId(@Param("productInfoId") Integer productInfoId);
}
