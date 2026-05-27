package org.ashroev.repository;

import org.ashroev.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, Integer> {
    boolean existsByCategoryId(Integer categoryId);
}
