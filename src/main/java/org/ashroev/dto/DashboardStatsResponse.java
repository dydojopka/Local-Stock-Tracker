package org.ashroev.dto;

public record DashboardStatsResponse(
        long totalProducts,
        Double totalQuantity,
        long expiredCount,
        Double expiredQuantity,
        long activeListsCount,
        long lowStockCount,
        int healthScore,
        ChartDataResponse<Double> categoryChart,
        ChartDataResponse<Long> expiryChart,
        DisposalChartResponse disposalChart
) {
}
