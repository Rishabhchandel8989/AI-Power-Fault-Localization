package com.powerfault.backend.repository;

import com.powerfault.backend.entity.TelemetryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryHistoryRepository
        extends JpaRepository<TelemetryHistory, Long> {

    List<TelemetryHistory> findByDeviceId(Long deviceId);

}