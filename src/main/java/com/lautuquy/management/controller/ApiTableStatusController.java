package com.lautuquy.management.controller;

import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.service.TableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class ApiTableStatusController {

    private final TableService tableService;

    public ApiTableStatusController(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * Endpoint phục vụ AJAX polling NFR-03: Trả về trạng thái thời gian thực của tất cả các bàn.
     */
    @GetMapping("/status")
    public List<Map<String, Object>> getTableStatuses() {
        return tableService.getAllTables().stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("tableNumber", t.getTableNumber());
            map.put("status", t.getStatus().name());
            map.put("capacity", t.getTableType() != null ? t.getTableType().getCapacity() : 0);
            map.put("tableClass", t.getTableType() != null ? t.getTableType().getTableClass().name() : "REGULAR");
            return map;
        }).toList();
    }
}
