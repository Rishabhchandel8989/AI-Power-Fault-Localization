package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Pole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoleRepository extends JpaRepository<Pole, Long> {

    Optional<Pole> findByPoleCode(String poleCode);

    List<Pole> findByTransformerId(Long transformerId);

    List<Pole> findByHasDevice(Boolean hasDevice);

}