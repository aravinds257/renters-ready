package com.rentersready.repository;

import com.rentersready.model.Tenancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenancyRepository extends JpaRepository<Tenancy, UUID> {
    List<Tenancy> findByPropertyIdOrderByStartDateDesc(UUID propertyId);
    Optional<Tenancy> findByPropertyIdAndIsActiveTrue(UUID propertyId);
    List<Tenancy> findByPropertyUserIdAndIsActiveTrue(UUID userId);
}
