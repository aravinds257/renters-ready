package com.rentersready.repository;

import com.rentersready.model.ComplianceCheck;
import com.rentersready.model.enums.CheckStatus;
import com.rentersready.model.enums.CheckType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, UUID> {
    List<ComplianceCheck> findByPropertyId(UUID propertyId);
    Optional<ComplianceCheck> findByPropertyIdAndCheckType(UUID propertyId, CheckType checkType);
    List<ComplianceCheck> findByPropertyUserIdAndStatusIn(UUID userId, List<CheckStatus> statuses);
}
