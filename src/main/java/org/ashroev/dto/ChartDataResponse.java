package org.ashroev.dto;

import java.util.List;

public record ChartDataResponse<T extends Number>(
        List<String> labels,
        List<T> data
) {
}
