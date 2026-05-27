package org.ashroev.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductInfoCreateRequest {

    @NotBlank(message = "Укажите название продукта")
    @Size(max = 255, message = "Название слишком длинное")
    private String name;

    @Size(max = 64, message = "Штрихкод слишком длинный")
    private String barcode;

    @NotNull(message = "Укажите минимальный остаток")
    @DecimalMin(value = "0.0", message = "Минимальный остаток не может быть отрицательным")
    private Double minQuantity;

    @NotNull(message = "Выберите категорию")
    @Min(value = 1, message = "Некорректная категория")
    private Integer categoryId;
}
