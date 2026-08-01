package com.powerfault.backend.repository;

import com.powerfault.backend.entity.NetworkConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkConnectionRepository
        extends JpaRepository<NetworkConnection, Long> {

    List<NetworkConnection> findByFromPoleId(Long poleId);

    List<NetworkConnection> findByToPoleId(Long poleId);

}