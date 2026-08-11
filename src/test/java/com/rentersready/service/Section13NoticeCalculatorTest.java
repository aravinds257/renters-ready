package com.rentersready.service;

import com.rentersready.model.LegalNotice;
import com.rentersready.model.Tenancy;
import com.rentersready.model.enums.NoticeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Section13NoticeCalculatorTest {

    private Section13NoticeCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new Section13NoticeCalculator();
    }

    @Test
    public void testCalculateEarliestStartingDate_Requires2MonthsNotice() {
        LocalDate serviceDate = LocalDate.of(2026, 5, 1);
        LocalDate earliestDate = calculator.calculateEarliestStartingDate(serviceDate);

        assertEquals(LocalDate.of(2026, 7, 1), earliestDate);
    }

    @Test
    public void testValidateSection13Eligibility_First12Months_ThrowsException() {
        Tenancy tenancy = Tenancy.builder()
                .startDate(LocalDate.of(2026, 1, 1))
                .monthlyRent(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        // Proposed starting date is 6 months after tenancy start date -> should fail
        LocalDate proposedDate = LocalDate.of(2026, 7, 1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.validateSection13Eligibility(tenancy, new ArrayList<>(), proposedDate);
        });

        assertTrue(exception.getMessage().contains("first 12 months"));
    }

    @Test
    public void testValidateSection13Eligibility_MoreThanOnceIn12Months_ThrowsException() {
        Tenancy tenancy = Tenancy.builder()
                .startDate(LocalDate.of(2024, 1, 1))
                .monthlyRent(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        List<LegalNotice> existingNotices = new ArrayList<>();
        existingNotices.add(LegalNotice.builder()
                .noticeType(NoticeType.SECTION_13)
                .startingDate(LocalDate.of(2026, 1, 1))
                .build());

        // Proposed new starting date is only 6 months after previous increase -> should fail
        LocalDate proposedDate = LocalDate.of(2026, 7, 1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.validateSection13Eligibility(tenancy, existingNotices, proposedDate);
        });

        assertTrue(exception.getMessage().contains("once in any 12-month period"));
    }

    @Test
    public void testCalculatePercentageIncrease() {
        BigDecimal currentRent = new BigDecimal("1000.00");
        BigDecimal proposedRent = new BigDecimal("1150.00");

        BigDecimal percentage = calculator.calculatePercentageIncrease(currentRent, proposedRent);

        assertEquals(new BigDecimal("15.00"), percentage);
    }
}
