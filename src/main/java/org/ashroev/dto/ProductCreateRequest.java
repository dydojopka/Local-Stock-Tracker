package org.ashroev.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProductCreateRequest {

    @NotNull(message = "Выберите продукт из справочника")
    @Min(value = 1, message = "Некорректный продукт")
    private Integer productInfoId;

    @NotNull(message = "Укажите количество")
    @DecimalMin(value = "0.01", message = "Количество должно быть больше нуля")
    private Double quantity;

    @NotNull(message = "Укажите срок годности")
    @FutureOrPresent(message = "Срок годности не может быть в прошлом")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
}
