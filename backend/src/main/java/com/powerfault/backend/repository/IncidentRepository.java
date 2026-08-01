package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Incident;
import com.powerfault.backend.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository
        extends JpaRepository<Incident, Long> {

    List<Incident> findByStatus(IncidentStatus status);

}