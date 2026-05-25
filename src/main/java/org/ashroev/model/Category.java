package org.ashroev.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    // Пустой конструктор (обязателен для Hibernate)
    public Category() {
    }

    // --- Геттеры и Сеттеры ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    // Тот самый метод, на отсутствие которого ругалась Java
    public void setDescription(String description) {
        this.description = description;
    }
}