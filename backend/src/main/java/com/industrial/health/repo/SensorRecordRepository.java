package com.industrial.health.repo;

import com.industrial.health.model.SensorRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRecordRepository extends JpaRepository<SensorRecord, Long> {
    long countByLabel(String label);
}
