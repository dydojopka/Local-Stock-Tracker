package org.ashroev.dto;

import java.util.List;

public record DisposalChartResponse(
        List<String> labels,
        List<Long> consumedData,
        List<Long> expiredData
) {
}
