package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * Список покупок
 */

@Entity
@Table(name = "shopping_lists")
@Data
@NoArgsConstructor
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(nullable = false)
    private String status; // Будет хранить статус списка ("PENDING", "APPROVED")

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingListItem> items = new ArrayList<>();

    // Количество позиций
    public Integer totalItems() {
        return items != null ? items.size() : 0;
    }
}