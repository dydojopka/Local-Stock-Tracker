package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/*
 * Партия товара на складе
 */

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "added_date", nullable = false)
    private LocalDate addedDate;

    // Связь с ProductInfo
    @ManyToOne
    @JoinColumn(name = "product_info_id", nullable = false)
    private ProductInfo productInfo;

    // Проверка просрочки
    public Boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}