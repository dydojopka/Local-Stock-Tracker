package org.ashroev.controller;

import lombok.RequiredArgsConstructor;
import org.ashroev.dto.DashboardStatsResponse;
import org.ashroev.service.InventoryManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final InventoryManager inventoryManager;

    @GetMapping("/stats")
    public DashboardStatsResponse getDashboardStats() {
        return inventoryManager.getDashboardStatsResponse();
    }
}
