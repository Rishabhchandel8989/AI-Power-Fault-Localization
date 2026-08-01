package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Device;
import com.powerfault.backend.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceId(String deviceId);

    List<Device> findByStatus(DeviceStatus status);

}