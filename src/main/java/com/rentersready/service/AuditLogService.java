package com.rentersready.service;

import com.rentersready.model.AuditLog;
import com.rentersready.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> getAuditLogsForUser(UUID userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> getAuditLogsForProperty(UUID propertyId) {
        return auditLogRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
    }

    @Transactional
    public AuditLog logCommunication(UUID userId, UUID propertyId, String actionType, String description) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .propertyId(propertyId)
                .actionType(actionType != null ? actionType.trim() : "COMMUNICATION_LOGGED")
                .description(description.trim())
                .build();

        return auditLogRepository.save(log);
    }
}
