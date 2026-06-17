package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findAllByOrderByMovementDateDesc();

    List<StockMovement> findAllByProduct_IdOrderByMovementDateDesc(Long productId);
}
