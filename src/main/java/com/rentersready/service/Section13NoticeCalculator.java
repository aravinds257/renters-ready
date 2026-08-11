package com.rentersready.service;

import com.rentersready.model.LegalNotice;
import com.rentersready.model.Tenancy;
import com.rentersready.model.enums.NoticeType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Calculator & Validator enforcing UK Renters' Rights Act statutory rules for Section 13 Rent Review Notices (Form 4).
 */
@Service
public class Section13NoticeCalculator {

    /**
     * Calculates the minimum valid starting date for a Section 13 rent increase notice.
     * Under the Renters' Rights Act, at least 2 full months' advance notice is mandatory for monthly tenancies.
     */
    public LocalDate calculateEarliestStartingDate(LocalDate serviceDate) {
        if (serviceDate == null) {
            serviceDate = LocalDate.now();
        }
        // Add 2 full calendar months notice period
        return serviceDate.plusMonths(2);
    }

    /**
     * Verifies if a Section 13 rent increase is allowed on this tenancy.
     * Rent increases can occur at most ONCE in any 12-month period.
     */
    public void validateSection13Eligibility(Tenancy tenancy, List<LegalNotice> existingNotices, LocalDate proposedStartingDate) {
        if (!tenancy.isActive()) {
            throw new IllegalArgumentException("Cannot issue a Section 13 rent review notice on an inactive tenancy.");
        }

        // Check if tenancy started less than 12 months ago
        if (tenancy.getStartDate().plusMonths(12).isAfter(proposedStartingDate)) {
            throw new IllegalArgumentException("Under the Renters' Rights Act, rent cannot be increased within the first 12 months of a tenancy.");
        }

        // Check previous Section 13 notices served on this tenancy
        for (LegalNotice notice : existingNotices) {
            if (notice.getNoticeType() == NoticeType.SECTION_13) {
                LocalDate lastIncreaseDate = notice.getStartingDate();
                if (lastIncreaseDate.plusMonths(12).isAfter(proposedStartingDate)) {
                    throw new IllegalArgumentException("Under the Renters' Rights Act, rent cannot be increased more than once in any 12-month period. Last increase date: " + lastIncreaseDate);
                }
            }
        }
    }

    /**
     * Calculate percentage rent increase.
     */
    public BigDecimal calculatePercentageIncrease(BigDecimal currentRent, BigDecimal proposedRent) {
        if (currentRent == null || currentRent.compareTo(BigDecimal.ZERO) <= 0 || proposedRent == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal difference = proposedRent.subtract(currentRent);
        return difference.divide(currentRent, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }
}
