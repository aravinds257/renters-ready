package com.rentersready.repository;

import com.rentersready.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    List<Property> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Property> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
