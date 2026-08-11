package com.rentersready.controller;

import com.rentersready.model.ComplianceCheck;
import com.rentersready.model.Property;
import com.rentersready.model.Tenancy;
import com.rentersready.model.User;
import com.rentersready.model.enums.CheckStatus;
import com.rentersready.service.ComplianceService;
import com.rentersready.service.PropertyService;
import com.rentersready.service.TenancyService;
import com.rentersready.service.UserService;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final TenancyService tenancyService;
    private final ComplianceService complianceService;

    public DashboardController(UserService userService,
                               PropertyService propertyService,
                               TenancyService tenancyService,
                               ComplianceService complianceService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.tenancyService = tenancyService;
        this.complianceService = complianceService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Property> properties = propertyService.getPropertiesForUser(user.getId());
        List<Tenancy> activeTenancies = tenancyService.getActiveTenanciesForUser(user.getId());
        List<ComplianceCheck> complianceAlerts = complianceService.getAlertsForUser(user.getId());

        BigDecimal totalMonthlyRentRoll = activeTenancies.stream()
                .map(Tenancy::getMonthlyRent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long expiredCount = complianceAlerts.stream().filter(c -> c.getStatus() == CheckStatus.EXPIRED || c.getStatus() == CheckStatus.MISSING).count();
        long expiringSoonCount = complianceAlerts.stream().filter(c -> c.getStatus() == CheckStatus.EXPIRING_SOON).count();

        DashboardSummary summary = DashboardSummary.builder()
                .totalProperties(properties.size())
                .activeTenancies(activeTenancies.size())
                .totalMonthlyRentRoll(totalMonthlyRentRoll)
                .expiredCount(expiredCount)
                .expiringSoonCount(expiringSoonCount)
                .recentProperties(properties.stream().limit(5).collect(Collectors.toList()))
                .criticalAlerts(complianceAlerts)
                .build();

        model.addAttribute("summary", summary);
        return "dashboard";
    }

    @Data
    @Builder
    public static class DashboardSummary {
        private int totalProperties;
        private int activeTenancies;
        private BigDecimal totalMonthlyRentRoll;
        private long expiredCount;
        private long expiringSoonCount;
        private List<Property> recentProperties;
        private List<ComplianceCheck> criticalAlerts;
    }
}
