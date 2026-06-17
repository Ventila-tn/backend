package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {
    List<StockBatch> findByProductId(Long productId);

    Optional<StockBatch> findByProductIdAndBatchNumber(Long productId, String batchNumber);
    
    List<StockBatch> findByProductIdOrderByExpirationDateAsc(Long productId);
}
