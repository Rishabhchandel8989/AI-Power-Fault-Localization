package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Substation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubstationRepository extends JpaRepository<Substation, Long> {

    Optional<Substation> findBySubstationCode(String substationCode);

}