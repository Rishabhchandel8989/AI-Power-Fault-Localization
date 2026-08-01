package com.powerfault.backend.repository;

import com.powerfault.backend.entity.Transformer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransformerRepository extends JpaRepository<Transformer, Long> {

    Optional<Transformer> findByTransformerCode(String transformerCode);

    List<Transformer> findByFeederId(Long feederId);

    List<Transformer> findByTopologyKnown(Boolean topologyKnown);

}