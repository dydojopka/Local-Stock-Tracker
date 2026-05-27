package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Справочник продуктов
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

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}