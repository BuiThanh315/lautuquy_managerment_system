package com.lautuquy.management.service;

import com.lautuquy.management.dto.response.TableOptionDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TableLockService {
    void lockTable(Long tableId, String sessionId);
    void unlockTable(Long tableId, String sessionId);
    void releaseSessionLock(String sessionId);
    boolean isLockedByOther(Long tableId, String sessionId);
    boolean isLockedByMe(Long tableId, String sessionId);
    List<TableOptionDto> getTableOptions(Long tableTypeId, LocalDate bookingDate, LocalTime bookingTime, String sessionId);
}
