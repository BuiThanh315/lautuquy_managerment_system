package com.lautuquy.management.service.impl;

import com.lautuquy.management.dto.request.TableRequest;
import com.lautuquy.management.dto.request.TableTypeRequest;
import com.lautuquy.management.entity.RestaurantTable;
import com.lautuquy.management.entity.TableType;
import com.lautuquy.management.exception.ResourceNotFoundException;
import com.lautuquy.management.repository.RestaurantTableRepository;
import com.lautuquy.management.repository.TableTypeRepository;
import com.lautuquy.management.service.TableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableServiceImpl implements TableService {

    private final TableTypeRepository tableTypeRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    public TableServiceImpl(TableTypeRepository tableTypeRepository, RestaurantTableRepository restaurantTableRepository) {
        this.tableTypeRepository = tableTypeRepository;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    public List<TableType> getAllTableTypes() {
        return tableTypeRepository.findAll();
    }

    @Override
    public TableType getTableTypeById(Long id) {
        return tableTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loại bàn", id));
    }

    @Override
    @Transactional
    public TableType createTableType(TableTypeRequest request) {
        TableType tableType = new TableType();
        tableType.setCapacity(request.getCapacity());
        tableType.setTableClass(request.getTableClass());
        return tableTypeRepository.save(tableType);
    }

    @Override
    @Transactional
    public void deleteTableType(Long id) {
        TableType tableType = getTableTypeById(id);
        tableTypeRepository.delete(tableType);
    }

    @Override
    public List<RestaurantTable> getAllTables() {
        return restaurantTableRepository.findAll();
    }

    @Override
    public RestaurantTable getTableById(Long id) {
        return restaurantTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn ăn", id));
    }

    @Override
    @Transactional
    public RestaurantTable createTable(TableRequest request) {
        if (restaurantTableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("Số bàn '" + request.getTableNumber() + "' đã tồn tại.");
        }
        TableType tableType = getTableTypeById(request.getTableTypeId());

        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(request.getTableNumber());
        table.setTableType(tableType);
        table.setStatus(request.getStatus() != null ? request.getStatus() : RestaurantTable.Status.EMPTY);

        return restaurantTableRepository.save(table);
    }

    @Override
    @Transactional
    public RestaurantTable updateTable(Long id, TableRequest request) {
        RestaurantTable table = getTableById(id);
        if (!table.getTableNumber().equalsIgnoreCase(request.getTableNumber())
                && restaurantTableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new IllegalArgumentException("Số bàn '" + request.getTableNumber() + "' đã tồn tại.");
        }
        TableType tableType = getTableTypeById(request.getTableTypeId());

        table.setTableNumber(request.getTableNumber());
        table.setTableType(tableType);
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }

        return restaurantTableRepository.save(table);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = getTableById(id);
        restaurantTableRepository.delete(table);
    }
}
