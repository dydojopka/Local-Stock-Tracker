package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/*
Класс представляет физическую партию товара.
Здесь хранится информация о конкретной упаковке в холодильнике:
её количество, дата добавления и срок годности.
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

    // Связь ProductInfo (1) - Product (*)
    // Одно описание может относиться к нескольким физическим пачкам с разными сроками
    @ManyToOne
    @JoinColumn(name = "product_info_id", nullable = false)
    private ProductInfo productInfo;

    // Метод для проверки, истёк ли срок годности
    public Boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }
}