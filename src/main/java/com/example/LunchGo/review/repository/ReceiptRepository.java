package com.example.LunchGo.review.repository;

import com.example.LunchGo.review.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
