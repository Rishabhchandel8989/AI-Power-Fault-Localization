package com.powerfault.backend.repository;
import com.powerfault.backend.entity.Device;
import java.util.Optional;
import com.powerfault.backend.entity.DeviceTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTelemetryRepository
        extends JpaRepository<DeviceTelemetry, Long> {
        Optional<DeviceTelemetry> findByDevice(Device device);
}