package org.ashroev.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductMinQuantityRequest {

    @NotNull(message = "Не выбран продукт")
    @Min(value = 1, message = "Некорректный продукт")
    private Integer id;

    @NotNull(message = "Укажите минимальный остаток")
    @DecimalMin(value = "0.0", message = "Минимальный остаток не может быть отрицательным")
    private Double minQuantity;
}
