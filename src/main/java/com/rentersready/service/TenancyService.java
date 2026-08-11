package com.rentersready.service;

import com.rentersready.model.Property;
import com.rentersready.model.Tenancy;
import com.rentersready.repository.PropertyRepository;
import com.rentersready.repository.TenancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenancyService {

    private final TenancyRepository tenancyRepository;
    private final PropertyRepository propertyRepository;

    public TenancyService(TenancyRepository tenancyRepository, PropertyRepository propertyRepository) {
        this.tenancyRepository = tenancyRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Tenancy> getActiveTenanciesForUser(UUID userId) {
        return tenancyRepository.findByPropertyUserIdAndIsActiveTrue(userId);
    }

    public List<Tenancy> getTenanciesForProperty(UUID propertyId) {
        return tenancyRepository.findByPropertyIdOrderByStartDateDesc(propertyId);
    }

    public Optional<Tenancy> getTenancyById(UUID tenancyId, UUID userId) {
        return tenancyRepository.findById(tenancyId)
                .filter(t -> t.getProperty().getUser().getId().equals(userId));
    }

    @Transactional
    public Tenancy createTenancy(UUID propertyId, UUID userId, String tenantName,
                                 String tenantEmail, String tenantPhone, LocalDate startDate,
                                 BigDecimal monthlyRent, int rentDueDay, BigDecimal depositAmount,
                                 String depositSchemeName, String depositReference) {

        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found or access denied"));

        // Deactivate existing active tenancies on this property
        tenancyRepository.findByPropertyIdAndIsActiveTrue(propertyId).ifPresent(t -> {
            t.setActive(false);
            tenancyRepository.save(t);
        });

        Tenancy tenancy = Tenancy.builder()
                .property(property)
                .tenantName(tenantName.trim())
                .tenantEmail(tenantEmail != null ? tenantEmail.toLowerCase().trim() : null)
                .tenantPhone(tenantPhone != null ? tenantPhone.trim() : null)
                .startDate(startDate)
                .monthlyRent(monthlyRent)
                .rentDueDay(rentDueDay)
                .depositAmount(depositAmount)
                .depositSchemeName(depositSchemeName != null ? depositSchemeName.trim() : null)
                .depositReference(depositReference != null ? depositReference.trim() : null)
                .isActive(true)
                .build();

        return tenancyRepository.save(tenancy);
    }

    @Transactional
    public void endTenancy(UUID tenancyId, UUID userId) {
        Tenancy tenancy = tenancyRepository.findById(tenancyId)
                .filter(t -> t.getProperty().getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Tenancy not found or access denied"));

        tenancy.setActive(false);
        tenancyRepository.save(tenancy);
    }
}
