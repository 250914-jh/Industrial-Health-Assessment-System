package com.industrial.health.repo;

import com.industrial.health.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByLevel(String level);
}
