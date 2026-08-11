package com.rentersready.controller;

import com.rentersready.model.Property;
import com.rentersready.model.Tenancy;
import com.rentersready.model.User;
import com.rentersready.service.PropertyService;
import com.rentersready.service.TenancyService;
import com.rentersready.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tenancies")
public class TenancyController {

    private final TenancyService tenancyService;
    private final PropertyService propertyService;
    private final UserService userService;

    public TenancyController(TenancyService tenancyService,
                             PropertyService propertyService,
                             UserService userService) {
        this.tenancyService = tenancyService;
        this.propertyService = propertyService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = getUser(auth);
        List<Tenancy> tenancies = tenancyService.getActiveTenanciesForUser(user.getId());
        model.addAttribute("tenancies", tenancies);
        return "tenancy/list";
    }

    @GetMapping("/add")
    public String addForm(@RequestParam(required = false) UUID propertyId, Authentication auth, Model model) {
        User user = getUser(auth);
        List<Property> properties = propertyService.getPropertiesForUser(user.getId());

        TenancyForm form = new TenancyForm();
        if (propertyId != null) {
            form.setPropertyId(propertyId);
        }

        model.addAttribute("tenancyForm", form);
        model.addAttribute("properties", properties);
        return "tenancy/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("tenancyForm") TenancyForm form,
                      BindingResult result,
                      Authentication auth,
                      Model model,
                      RedirectAttributes redirectAttributes) {

        User user = getUser(auth);

        if (result.hasErrors()) {
            model.addAttribute("properties", propertyService.getPropertiesForUser(user.getId()));
            return "tenancy/add";
        }

        Tenancy tenancy = tenancyService.createTenancy(
                form.getPropertyId(),
                user.getId(),
                form.getTenantName(),
                form.getTenantEmail(),
                form.getTenantPhone(),
                form.getStartDate(),
                form.getMonthlyRent(),
                form.getRentDueDay(),
                form.getDepositAmount(),
                form.getDepositSchemeName(),
                form.getDepositReference()
        );

        redirectAttributes.addFlashAttribute("success", "Tenancy registered successfully!");
        return "redirect:/properties/" + tenancy.getProperty().getId();
    }

    @PostMapping("/{id}/end")
    public String endTenancy(@PathVariable UUID id, Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getUser(auth);
        try {
            tenancyService.endTenancy(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Tenancy marked as ended.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tenancies";
    }

    private User getUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class TenancyForm {
        @NotNull(message = "Property is required")
        private UUID propertyId;

        @NotBlank(message = "Tenant name is required")
        private String tenantName;

        private String tenantEmail;
        private String tenantPhone;

        @NotNull(message = "Start date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startDate;

        @NotNull(message = "Monthly rent is required")
        private BigDecimal monthlyRent;

        @Min(value = 1, message = "Rent due day must be between 1 and 31")
        @Max(value = 31, message = "Rent due day must be between 1 and 31")
        private int rentDueDay = 1;

        private BigDecimal depositAmount;
        private String depositSchemeName;
        private String depositReference;

        public UUID getPropertyId() { return propertyId; }
        public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }

        public String getTenantName() { return tenantName; }
        public void setTenantName(String tenantName) { this.tenantName = tenantName; }

        public String getTenantEmail() { return tenantEmail; }
        public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }

        public String getTenantPhone() { return tenantPhone; }
        public void setTenantPhone(String tenantPhone) { this.tenantPhone = tenantPhone; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public BigDecimal getMonthlyRent() { return monthlyRent; }
        public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }

        public int getRentDueDay() { return rentDueDay; }
        public void setRentDueDay(int rentDueDay) { this.rentDueDay = rentDueDay; }

        public BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

        public String getDepositSchemeName() { return depositSchemeName; }
        public void setDepositSchemeName(String depositSchemeName) { this.depositSchemeName = depositSchemeName; }

        public String getDepositReference() { return depositReference; }
        public void setDepositReference(String depositReference) { this.depositReference = depositReference; }
    }
}
