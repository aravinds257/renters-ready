package com.rentersready.service;

import com.rentersready.model.AuditLog;
import com.rentersready.model.LegalNotice;
import com.rentersready.model.Tenancy;
import com.rentersready.model.enums.NoticeStatus;
import com.rentersready.model.enums.NoticeType;
import com.rentersready.repository.AuditLogRepository;
import com.rentersready.repository.LegalNoticeRepository;
import com.rentersready.repository.TenancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LegalNoticeService {

    private final LegalNoticeRepository legalNoticeRepository;
    private final TenancyRepository tenancyRepository;
    private final AuditLogRepository auditLogRepository;
    private final Section13NoticeCalculator section13Calculator;

    public LegalNoticeService(LegalNoticeRepository legalNoticeRepository,
                              TenancyRepository tenancyRepository,
                              AuditLogRepository auditLogRepository,
                              Section13NoticeCalculator section13Calculator) {
        this.legalNoticeRepository = legalNoticeRepository;
        this.tenancyRepository = tenancyRepository;
        this.auditLogRepository = auditLogRepository;
        this.section13Calculator = section13Calculator;
    }

    public List<LegalNotice> getNoticesForUser(UUID userId) {
        return legalNoticeRepository.findByTenancyPropertyUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<LegalNotice> getNoticeById(UUID noticeId, UUID userId) {
        return legalNoticeRepository.findById(noticeId)
                .filter(n -> n.getTenancy().getProperty().getUser().getId().equals(userId));
    }

    @Transactional
    public LegalNotice createSection13Notice(UUID tenancyId, UUID userId, BigDecimal proposedRent, LocalDate startingDate) {
        Tenancy tenancy = tenancyRepository.findById(tenancyId)
                .filter(t -> t.getProperty().getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found or access denied"));

        List<LegalNotice> existingNotices = legalNoticeRepository.findByTenancyIdOrderByCreatedAtDesc(tenancyId);

        // Validate Section 13 eligibility under Renters' Rights Act rules
        section13Calculator.validateSection13Eligibility(tenancy, existingNotices, startingDate);

        LegalNotice notice = LegalNotice.builder()
                .tenancy(tenancy)
                .noticeType(NoticeType.SECTION_13)
                .currentRent(tenancy.getMonthlyRent())
                .proposedRent(proposedRent)
                .startingDate(startingDate)
                .status(NoticeStatus.DRAFT)
                .build();

        LegalNotice savedNotice = legalNoticeRepository.save(notice);

        // Audit log
        AuditLog audit = AuditLog.builder()
                .userId(userId)
                .propertyId(tenancy.getProperty().getId())
                .actionType("SECTION_13_NOTICE_CREATED")
                .description("Generated Section 13 Rent Review Notice for " + tenancy.getTenantName() + " (Proposed rent: £" + proposedRent + ")")
                .build();
        auditLogRepository.save(audit);

        return savedNotice;
    }

    @Transactional
    public LegalNotice createSection8Notice(UUID tenancyId, UUID userId, LocalDate startingDate, String groundsJson) {
        Tenancy tenancy = tenancyRepository.findById(tenancyId)
                .filter(t -> t.getProperty().getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found or access denied"));

        LegalNotice notice = LegalNotice.builder()
                .tenancy(tenancy)
                .noticeType(NoticeType.SECTION_8)
                .currentRent(tenancy.getMonthlyRent())
                .startingDate(startingDate)
                .groundsJson(groundsJson)
                .status(NoticeStatus.DRAFT)
                .build();

        LegalNotice savedNotice = legalNoticeRepository.save(notice);

        // Audit log
        AuditLog audit = AuditLog.builder()
                .userId(userId)
                .propertyId(tenancy.getProperty().getId())
                .actionType("SECTION_8_NOTICE_CREATED")
                .description("Generated Section 8 Possession Notice for " + tenancy.getTenantName())
                .build();
        auditLogRepository.save(audit);

        return savedNotice;
    }

    @Transactional
    public LegalNotice markNoticeServed(UUID noticeId, UUID userId, String servedMethod) {
        LegalNotice notice = legalNoticeRepository.findById(noticeId)
                .filter(n -> n.getTenancy().getProperty().getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Notice not found or access denied"));

        notice.setStatus(NoticeStatus.SERVED);
        notice.setServedAt(LocalDateTime.now());
        notice.setServedMethod(servedMethod != null ? servedMethod.trim() : "FIRST_CLASS_POST");

        // Audit log
        AuditLog audit = AuditLog.builder()
                .userId(userId)
                .propertyId(notice.getTenancy().getProperty().getId())
                .actionType("NOTICE_SERVED")
                .description("Marked " + notice.getNoticeType().getDisplayLabel() + " as served to " + notice.getTenancy().getTenantName() + " via " + notice.getServedMethod())
                .build();
        auditLogRepository.save(audit);

        return legalNoticeRepository.save(notice);
    }
}
