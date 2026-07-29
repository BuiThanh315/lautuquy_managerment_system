package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.response.TableOptionDto;
import com.lautuquy.management.entity.Booking;
import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.repository.BookingRepository;
import com.lautuquy.management.repository.RestaurantTableRepository;
import com.lautuquy.management.service.TableLockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TableLockServiceImpl implements TableLockService {

    private static final long LOCK_TIMEOUT_MS = 5 * 60 * 1000; // 5 phút

    private static class LockInfo {
        final String sessionId;
        final long lockedAt;

        LockInfo(String sessionId, long lockedAt) {
            this.sessionId = sessionId;
            this.lockedAt = lockedAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - lockedAt > LOCK_TIMEOUT_MS;
        }
    }

    private final Map<Long, LockInfo> tableLocks = new ConcurrentHashMap<>();
    private final RestaurantTableRepository restaurantTableRepository;
    private final BookingRepository bookingRepository;

    public TableLockServiceImpl(RestaurantTableRepository restaurantTableRepository,
                                BookingRepository bookingRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public synchronized void lockTable(Long tableId, String sessionId) {
        if (sessionId == null) return;

        // Tự động giải phóng bàn đã chọn trước đó của session này (không cần chờ 5 phút)
        releaseSessionLock(sessionId);

        if (tableId == null) return;

        LockInfo existing = tableLocks.get(tableId);
        if (existing != null && !existing.isExpired() && !existing.sessionId.equals(sessionId)) {
            throw new IllegalStateException("Bàn ăn này hiện đang được một khách hàng khác tạm khóa. Vui lòng chọn bàn khác.");
        }

        tableLocks.put(tableId, new LockInfo(sessionId, System.currentTimeMillis()));
    }

    @Override
    public synchronized void unlockTable(Long tableId, String sessionId) {
        if (tableId == null || sessionId == null) return;
        LockInfo lockInfo = tableLocks.get(tableId);
        if (lockInfo != null && lockInfo.sessionId.equals(sessionId)) {
            tableLocks.remove(tableId);
        }
    }

    @Override
    public synchronized void releaseSessionLock(String sessionId) {
        if (sessionId == null) return;
        tableLocks.entrySet().removeIf(entry -> entry.getValue().sessionId.equals(sessionId));
    }

    @Override
    public boolean isLockedByOther(Long tableId, String sessionId) {
        if (tableId == null) return false;
        LockInfo lockInfo = tableLocks.get(tableId);
        if (lockInfo == null || lockInfo.isExpired()) return false;
        return !lockInfo.sessionId.equals(sessionId);
    }

    @Override
    public boolean isLockedByMe(Long tableId, String sessionId) {
        if (tableId == null || sessionId == null) return false;
        LockInfo lockInfo = tableLocks.get(tableId);
        if (lockInfo == null || lockInfo.isExpired()) return false;
        return lockInfo.sessionId.equals(sessionId);
    }

    @Override
    public List<TableOptionDto> getTableOptions(Long tableTypeId, LocalDate bookingDate, LocalTime bookingTime, String sessionId) {
        List<RestaurantTable> tables = (tableTypeId != null) ?
                restaurantTableRepository.findByTableTypeId(tableTypeId) :
                restaurantTableRepository.findAll();

        List<Booking> activeBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == Booking.Status.PENDING ||
                        b.getStatus() == Booking.Status.CONFIRMED ||
                        b.getStatus() == Booking.Status.SEATED)
                .toList();

        List<TableOptionDto> options = new ArrayList<>();

        for (RestaurantTable table : tables) {
            boolean isBooked = false;

            // Kiểm tra bàn đã được gán đơn đặt bàn trên cùng ngày hoặc đang SEATED
            for (Booking b : activeBookings) {
                if (b.getAssignedTable() != null && b.getAssignedTable().getId().equals(table.getId())) {
                    if (b.getStatus() == Booking.Status.SEATED) {
                        isBooked = true;
                        break;
                    } else if (bookingDate != null && b.getBookingDate() != null && b.getBookingDate().isEqual(bookingDate)) {
                        isBooked = true;
                        break;
                    }
                }
            }

            // Bàn ăn ở trạng thái SERVING hoặc DIRTY cũng coi như bận
            if (table.getStatus() == RestaurantTable.Status.SERVING || table.getStatus() == RestaurantTable.Status.DIRTY) {
                isBooked = true;
            }

            boolean lockedByOther = isLockedByOther(table.getId(), sessionId);
            boolean lockedByMe = isLockedByMe(table.getId(), sessionId);

            boolean available = !isBooked && !lockedByOther;

            options.add(new TableOptionDto(
                    table.getId(),
                    table.getTableNumber(),
                    table.getTableType() != null ? table.getTableType().getCapacity() : 0,
                    table.getTableType() != null ? table.getTableType().getTableClass().name() : "REGULAR",
                    lockedByOther,
                    lockedByMe,
                    isBooked,
                    available
            ));
        }

        return options;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredLocks() {
        tableLocks.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
