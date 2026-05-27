package org.ashroev.repository;

import org.ashroev.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Integer> {
    boolean existsByProductInfoId(Integer productInfoId);
}
