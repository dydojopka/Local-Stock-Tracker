package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Класс описаний, который хранит постоянные метаданные товара.
Он включает наименование, штрих-код и минимально допустимый запас.
 */

@Entity
@Table(name = "product_info")
@Data
@NoArgsConstructor
public class ProductInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String barcode;

    @Column(name = "min_quantity", nullable = false)
    private Double minQuantity;

    // Связь Category (1) - ProductInfo (*)
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}