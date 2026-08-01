package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Feeder;
import com.powerfault.backend.enums.FeederStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeederRepository extends JpaRepository<Feeder, Long> {

    Optional<Feeder> findByFeederCode(String feederCode);

    List<Feeder> findByStatus(FeederStatus status);

}