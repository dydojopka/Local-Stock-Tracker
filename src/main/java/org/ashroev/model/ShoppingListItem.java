package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Отдельная строка внутри списка покупок.
Она связывает описание продукта (ProductInfo), который нужно купить,
с требуемым количеством и отметкой о том, куплен ли уже этот товар.
 */

@Entity
@Table(name = "shopping_list_items")
@Data
@NoArgsConstructor
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "target_quantity", nullable = false)
    private Double targetQuantity;

    @Column(name = "is_purchased", nullable = false)
    private Boolean isPurchased = false;

    // Связь с заголовком списка покупок
    @ManyToOne
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    // Связь с описанием продукта (что именно нужно купить)
    @ManyToOne
    @JoinColumn(name = "product_info_id", nullable = false)
    private ProductInfo productInfo;
}