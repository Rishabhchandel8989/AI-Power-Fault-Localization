package com.powerfault.backend.repository;

import com.powerfault.backend.entity.ScheduledOutage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledOutageRepository
        extends JpaRepository<ScheduledOutage, Long> {
}