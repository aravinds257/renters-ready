package com.rentersready.controller;

import com.rentersready.model.ComplianceCheck;
import com.rentersready.model.Property;
import com.rentersready.model.Tenancy;
import com.rentersready.model.User;
import com.rentersready.model.enums.PropertyType;
import com.rentersready.service.ComplianceService;
import com.rentersready.service.PropertyService;
import com.rentersready.service.TenancyService;
import com.rentersready.service.UserService;
import jakarta.validation.Valid;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserService userService;
    private final TenancyService tenancyService;
    private final ComplianceService complianceService;

    public PropertyController(PropertyService propertyService,
                              UserService userService,
                              TenancyService tenancyService,
                              ComplianceService complianceService) {
        this.propertyService = propertyService;
        this.userService = userService;
        this.tenancyService = tenancyService;
        this.complianceService = complianceService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = getUser(auth);
        List<Property> properties = propertyService.getPropertiesForUser(user.getId());
        model.addAttribute("properties", properties);
        return "property/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("propertyForm", new PropertyForm());
        model.addAttribute("propertyTypes", PropertyType.values());
        return "property/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("propertyForm") PropertyForm form,
                      BindingResult result,
                      Authentication auth,
                      Model model,
                      RedirectAttributes redirectAttributes) {

        User user = getUser(auth);

        if (result.hasErrors()) {
            model.addAttribute("propertyTypes", PropertyType.values());
            return "property/add";
        }

        Property property = propertyService.addProperty(
                user.getId(),
                form.getAddressLine1(),
                form.getAddressLine2(),
                form.getCity(),
                form.getPostcode(),
                form.getPropertyType(),
                form.getBedrooms(),
                form.getEpcRating(),
                form.getEpcExpiryDate(),
                form.getGasSafetyExpiryDate(),
                form.getEicrExpiryDate(),
                form.getPrsDatabaseRegNumber()
        );

        redirectAttributes.addFlashAttribute("success", "Property registered! Compliance tracking initialized.");
        return "redirect:/properties/" + property.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Authentication auth, Model model) {
        User user = getUser(auth);
        Property property = propertyService.getPropertyById(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        List<Tenancy> tenancies = tenancyService.getTenanciesForProperty(id);
        List<ComplianceCheck> complianceChecks = complianceService.getComplianceChecksForProperty(id);

        model.addAttribute("property", property);
        model.addAttribute("tenancies", tenancies);
        model.addAttribute("complianceChecks", complianceChecks);
        return "property/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, Authentication auth, RedirectAttributes redirectAttributes) {
        User user = getUser(auth);
        try {
            propertyService.deleteProperty(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Property deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/properties";
    }

    private User getUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class PropertyForm {
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;

        private String addressLine2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Postcode is required")
        private String postcode;

        @NotNull(message = "Property type is required")
        private PropertyType propertyType;

        @Min(value = 1, message = "Bedrooms must be at least 1")
        private int bedrooms = 1;

        private String epcRating;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate epcExpiryDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate gasSafetyExpiryDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate eicrExpiryDate;

        private String prsDatabaseRegNumber;

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getAddressLine2() { return addressLine2; }
        public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPostcode() { return postcode; }
        public void setPostcode(String postcode) { this.postcode = postcode; }

        public PropertyType getPropertyType() { return propertyType; }
        public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

        public int getBedrooms() { return bedrooms; }
        public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

        public String getEpcRating() { return epcRating; }
        public void setEpcRating(String epcRating) { this.epcRating = epcRating; }

        public LocalDate getEpcExpiryDate() { return epcExpiryDate; }
        public void setEpcExpiryDate(LocalDate epcExpiryDate) { this.epcExpiryDate = epcExpiryDate; }

        public LocalDate getGasSafetyExpiryDate() { return gasSafetyExpiryDate; }
        public void setGasSafetyExpiryDate(LocalDate gasSafetyExpiryDate) { this.gasSafetyExpiryDate = gasSafetyExpiryDate; }

        public LocalDate getEicrExpiryDate() { return eicrExpiryDate; }
        public void setEicrExpiryDate(LocalDate eicrExpiryDate) { this.eicrExpiryDate = eicrExpiryDate; }

        public String getPrsDatabaseRegNumber() { return prsDatabaseRegNumber; }
        public void setPrsDatabaseRegNumber(String prsDatabaseRegNumber) { this.prsDatabaseRegNumber = prsDatabaseRegNumber; }
    }
}
