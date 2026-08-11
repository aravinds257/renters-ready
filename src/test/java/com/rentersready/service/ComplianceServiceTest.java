package com.rentersready.service;

import com.rentersready.model.enums.CheckStatus;
import com.rentersready.model.enums.CheckType;
import com.rentersready.repository.ComplianceCheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComplianceServiceTest {

    private ComplianceService complianceService;

    @BeforeEach
    public void setUp() {
        ComplianceCheckRepository repository = Mockito.mock(ComplianceCheckRepository.class);
        complianceService = new ComplianceService(repository);
    }

    @Test
    public void testCalculateStatus_MissingDate_ReturnsMissing() {
        CheckStatus status = complianceService.calculateStatus(null, CheckType.GAS_SAFETY);
        assertEquals(CheckStatus.MISSING, status);
    }

    @Test
    public void testCalculateStatus_ExpiredDate_ReturnsExpired() {
        LocalDate expiredDate = LocalDate.now().minusDays(5);
        CheckStatus status = complianceService.calculateStatus(expiredDate, CheckType.GAS_SAFETY);
        assertEquals(CheckStatus.EXPIRED, status);
    }

    @Test
    public void testCalculateStatus_ExpiringWithin30Days_ReturnsExpiringSoon() {
        LocalDate expiringSoonDate = LocalDate.now().plusDays(15);
        CheckStatus status = complianceService.calculateStatus(expiringSoonDate, CheckType.GAS_SAFETY);
        assertEquals(CheckStatus.EXPIRING_SOON, status);
    }

    @Test
    public void testCalculateStatus_ValidDate_ReturnsValid() {
        LocalDate validDate = LocalDate.now().plusMonths(6);
        CheckStatus status = complianceService.calculateStatus(validDate, CheckType.GAS_SAFETY);
        assertEquals(CheckStatus.VALID, status);
    }
}
