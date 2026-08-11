package com.rentersready.service;

import com.rentersready.model.ComplianceCheck;
import com.rentersready.model.enums.CheckStatus;
import com.rentersready.model.enums.CheckType;
import com.rentersready.repository.ComplianceCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ComplianceService {

    private final ComplianceCheckRepository complianceCheckRepository;

    public ComplianceService(ComplianceCheckRepository complianceCheckRepository) {
        this.complianceCheckRepository = complianceCheckRepository;
    }

    public List<ComplianceCheck> getComplianceChecksForProperty(UUID propertyId) {
        return complianceCheckRepository.findByPropertyId(propertyId);
    }

    public List<ComplianceCheck> getAlertsForUser(UUID userId) {
        return complianceCheckRepository.findByPropertyUserIdAndStatusIn(
                userId,
                List.of(CheckStatus.EXPIRED, CheckStatus.EXPIRING_SOON, CheckStatus.MISSING)
        );
    }

    @Transactional
    public ComplianceCheck updateComplianceCheck(UUID checkId, LocalDate expiryDate, String certificateRef) {
        ComplianceCheck check = complianceCheckRepository.findById(checkId)
                .orElseThrow(() -> new IllegalArgumentException("Compliance check not found"));

        check.setExpiryDate(expiryDate);
        check.setCertificateRef(certificateRef != null ? certificateRef.trim() : null);
        check.setStatus(calculateStatus(expiryDate, check.getCheckType()));

        return complianceCheckRepository.save(check);
    }

    public CheckStatus calculateStatus(LocalDate expiryDate, CheckType checkType) {
        if (!checkType.isRequiresExpiryDate()) {
            return expiryDate != null ? CheckStatus.VALID : CheckStatus.MISSING;
        }

        if (expiryDate == null) {
            return CheckStatus.MISSING;
        }

        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            return CheckStatus.EXPIRED;
        } else if (expiryDate.isBefore(today.plusDays(30))) {
            return CheckStatus.EXPIRING_SOON;
        } else {
            return CheckStatus.VALID;
        }
    }
}
