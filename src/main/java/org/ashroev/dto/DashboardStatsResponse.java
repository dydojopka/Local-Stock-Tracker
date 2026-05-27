package org.ashroev.dto;

public record DashboardStatsResponse(
        long totalProducts,
        double totalQuantity,
        long expiredCount,
        double expiredQuantity,
        long activeListsCount,
        long lowStockCount,
        int healthScore,
        ChartDataResponse<Double> categoryChart,
        ChartDataResponse<Long> expiryChart,
        DisposalChartResponse disposalChart
) {
}
