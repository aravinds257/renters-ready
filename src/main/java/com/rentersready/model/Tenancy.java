package com.rentersready.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tenancies")
public class Tenancy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String tenantName;

    private String tenantEmail;

    private String tenantPhone;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private BigDecimal monthlyRent;

    @Column(nullable = false)
    private int rentDueDay;

    private BigDecimal depositAmount;

    private String depositSchemeName;

    private String depositReference;

    @Column(nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "tenancy", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LegalNotice> legalNotices = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
