package com.rentersready.repository;

import com.rentersready.model.LegalNotice;
import com.rentersready.model.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LegalNoticeRepository extends JpaRepository<LegalNotice, UUID> {
    List<LegalNotice> findByTenancyIdOrderByCreatedAtDesc(UUID tenancyId);
    List<LegalNotice> findByTenancyPropertyUserIdOrderByCreatedAtDesc(UUID userId);
    List<LegalNotice> findByTenancyIdAndNoticeTypeOrderByCreatedAtDesc(UUID tenancyId, NoticeType noticeType);
}
