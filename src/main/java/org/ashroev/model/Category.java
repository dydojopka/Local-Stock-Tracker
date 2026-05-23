package org.ashroev.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Класс группирует товары по логическим признакам. У него есть идентификатор и наименование
 */

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;
}