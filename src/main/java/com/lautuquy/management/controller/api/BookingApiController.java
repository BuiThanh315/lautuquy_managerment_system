package com.lautuquy.management.controller.api;

import com.lautuquy.management.dto.response.TableOptionDto;
import com.lautuquy.management.service.TableLockService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {

    private final TableLockService tableLockService;

    public BookingApiController(TableLockService tableLockService) {
        this.tableLockService = tableLockService;
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableOptionDto>> getAvailableTables(
            @RequestParam(name = "tableTypeId", required = false) Long tableTypeId,
            @RequestParam(name = "bookingDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate bookingDate,
            @RequestParam(name = "bookingTime", required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime bookingTime,
            HttpSession session) {

        List<TableOptionDto> tables = tableLockService.getTableOptions(
                tableTypeId,
                bookingDate != null ? bookingDate : LocalDate.now(),
                bookingTime,
                session.getId()
        );

        return ResponseEntity.ok(tables);
    }

    @PostMapping("/lock-table")
    public ResponseEntity<Map<String, Object>> lockTable(
            @RequestParam(name = "tableId", required = false) Long tableId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        try {
            tableLockService.lockTable(tableId, session.getId());
            response.put("success", true);
            response.put("message", tableId != null ? "Đã tạm khóa bàn ăn thành công." : "Đã giải phóng chọn bàn.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tạm khóa bàn ăn.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/unlock-table")
    public ResponseEntity<Map<String, Object>> unlockTable(
            @RequestParam(name = "tableId", required = false) Long tableId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        if (tableId != null) {
            tableLockService.unlockTable(tableId, session.getId());
        } else {
            tableLockService.releaseSessionLock(session.getId());
        }
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
