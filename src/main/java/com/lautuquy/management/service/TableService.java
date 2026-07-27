package com.lautuquy.management.service;

import com.lautuquy.management.dto.request.TableRequest;
import com.lautuquy.management.dto.request.TableTypeRequest;
import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.entity.TableType;

import java.util.List;

public interface TableService {
    List<TableType> getAllTableTypes();
    TableType getTableTypeById(Long id);
    TableType createTableType(TableTypeRequest request);
    void deleteTableType(Long id);

    List<RestaurantTable> getAllTables();
    RestaurantTable getTableById(Long id);
    RestaurantTable createTable(TableRequest request);
    RestaurantTable updateTable(Long id, TableRequest request);
    void deleteTable(Long id);
    RestaurantTable cleanTable(Long tableId);
}
