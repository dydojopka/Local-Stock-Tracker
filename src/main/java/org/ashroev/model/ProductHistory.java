package org.ashroev.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_history")
public class ProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связываем с общей информацией о продукте (чтобы знать имя, категорию и т.д.)
    @ManyToOne
    @JoinColumn(name = "product_info_id")
    private ProductInfo productInfo;

    // Сколько именно списали
    private double quantity;

    // Причина: USED или EXPIRED
    @Enumerated(EnumType.STRING)
    private WriteOffReason reason;

    // Точная дата и время списания (потребуется для графиков по дням/месяцам)
    private LocalDateTime actionDate;

    // Пустой конструктор для Hibernate
    public ProductHistory() {
    }

    public ProductHistory(ProductInfo productInfo, double quantity, WriteOffReason reason, LocalDateTime actionDate) {
        this.productInfo = productInfo;
        this.quantity = quantity;
        this.reason = reason;
        this.actionDate = actionDate;
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductInfo getProductInfo() { return productInfo; }
    public void setProductInfo(ProductInfo productInfo) { this.productInfo = productInfo; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public WriteOffReason getReason() { return reason; }
    public void setReason(WriteOffReason reason) { this.reason = reason; }

    public LocalDateTime getActionDate() { return actionDate; }
    public void setActionDate(LocalDateTime actionDate) { this.actionDate = actionDate; }
}
