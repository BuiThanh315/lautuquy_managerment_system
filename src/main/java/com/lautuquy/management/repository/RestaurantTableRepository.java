package com.lautuquy.management.repository;

import com.lautuquy.management.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(String tableNumber);
    boolean existsByTableNumber(String tableNumber);
    List<RestaurantTable> findByStatus(RestaurantTable.Status status);
    List<RestaurantTable> findByTableTypeId(Long tableTypeId);
}
