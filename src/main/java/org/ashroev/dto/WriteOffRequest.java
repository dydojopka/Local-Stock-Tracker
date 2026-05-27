package org.ashroev.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WriteOffRequest {

    @NotNull(message = "Не выбран продукт")
    @Min(value = 1, message = "Некорректный продукт")
    private Integer productId;

    @NotNull(message = "Укажите количество")
    @DecimalMin(value = "0.01", message = "Количество для списания должно быть больше нуля")
    private Double amount;

    @NotBlank(message = "Выберите причину списания")
    @Pattern(regexp = "USED|EXPIRED|DELETE", message = "Некорректная причина списания")
    private String reason = "USED";
}
