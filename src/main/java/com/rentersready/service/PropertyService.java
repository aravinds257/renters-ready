package com.rentersready.service;

import com.rentersready.model.ComplianceCheck;
import com.rentersready.model.Property;
import com.rentersready.model.User;
import com.rentersready.model.enums.CheckStatus;
import com.rentersready.model.enums.CheckType;
import com.rentersready.model.enums.PropertyType;
import com.rentersready.repository.ComplianceCheckRepository;
import com.rentersready.repository.PropertyRepository;
import com.rentersready.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ComplianceCheckRepository complianceCheckRepository;

    public PropertyService(PropertyRepository propertyRepository,
                           UserRepository userRepository,
                           ComplianceCheckRepository complianceCheckRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.complianceCheckRepository = complianceCheckRepository;
    }

    public List<Property> getPropertiesForUser(UUID userId) {
        return propertyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Property> getPropertyById(UUID id, UUID userId) {
        return propertyRepository.findByIdAndUserId(id, userId);
    }

    public long getPropertyCountForUser(UUID userId) {
        return propertyRepository.countByUserId(userId);
    }

    @Transactional
    public Property addProperty(UUID userId, String addressLine1, String addressLine2,
                                String city, String postcode, PropertyType propertyType,
                                int bedrooms, String epcRating, LocalDate epcExpiryDate,
                                LocalDate gasSafetyExpiryDate, LocalDate eicrExpiryDate,
                                String prsDatabaseRegNumber) {

        User user = userRepository.getReferenceById(userId);

        Property property = Property.builder()
                .user(user)
                .addressLine1(addressLine1.trim())
                .addressLine2(addressLine2 != null ? addressLine2.trim() : null)
                .city(city.trim())
                .postcode(postcode.toUpperCase().trim())
                .propertyType(propertyType)
                .bedrooms(bedrooms)
                .epcRating(epcRating != null ? epcRating.toUpperCase().trim() : null)
                .epcExpiryDate(epcExpiryDate)
                .gasSafetyExpiryDate(gasSafetyExpiryDate)
                .eicrExpiryDate(eicrExpiryDate)
                .prsDatabaseRegNumber(prsDatabaseRegNumber != null ? prsDatabaseRegNumber.trim() : null)
                .build();

        Property savedProperty = propertyRepository.save(property);

        // Auto-initialize compliance check records for safety tracking
        initComplianceCheck(savedProperty, CheckType.GAS_SAFETY, gasSafetyExpiryDate);
        initComplianceCheck(savedProperty, CheckType.EPC, epcExpiryDate);
        initComplianceCheck(savedProperty, CheckType.EICR, eicrExpiryDate);
        initComplianceCheck(savedProperty, CheckType.HOW_TO_RENT, null);
        initComplianceCheck(savedProperty, CheckType.DEPOSIT_INFO, null);

        return savedProperty;
    }

    @Transactional
    public void deleteProperty(UUID id, UUID userId) {
        Property property = propertyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found or access denied"));
        propertyRepository.delete(property);
    }

    private void initComplianceCheck(Property property, CheckType checkType, LocalDate expiryDate) {
        CheckStatus status = calculateStatus(expiryDate);

        ComplianceCheck check = ComplianceCheck.builder()
                .property(property)
                .checkType(checkType)
                .expiryDate(expiryDate)
                .status(status)
                .build();

        complianceCheckRepository.save(check);
    }

    private CheckStatus calculateStatus(LocalDate expiryDate) {
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
