package com.lautuquy.management.repository;

import com.lautuquy.management.entity.TableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableTypeRepository extends JpaRepository<TableType, Long> {
}
