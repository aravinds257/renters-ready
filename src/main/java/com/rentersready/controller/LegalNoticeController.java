package com.rentersready.controller;

import com.rentersready.model.LegalNotice;
import com.rentersready.model.Tenancy;
import com.rentersready.model.User;
import com.rentersready.service.LegalNoticeService;
import com.rentersready.service.Section13NoticeCalculator;
import com.rentersready.service.TenancyService;
import com.rentersready.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/notices")
public class LegalNoticeController {

    private final LegalNoticeService legalNoticeService;
    private final TenancyService tenancyService;
    private final UserService userService;
    private final Section13NoticeCalculator section13Calculator;

    public LegalNoticeController(LegalNoticeService legalNoticeService,
                                 TenancyService tenancyService,
                                 UserService userService,
                                 Section13NoticeCalculator section13Calculator) {
        this.legalNoticeService = legalNoticeService;
        this.tenancyService = tenancyService;
        this.userService = userService;
        this.section13Calculator = section13Calculator;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = getUser(auth);
        List<LegalNotice> notices = legalNoticeService.getNoticesForUser(user.getId());
        model.addAttribute("notices", notices);
        return "notice/list";
    }

    @GetMapping("/section13")
    public String section13Form(@RequestParam(required = false) UUID tenancyId, Authentication auth, Model model) {
        User user = getUser(auth);
        List<Tenancy> activeTenancies = tenancyService.getActiveTenanciesForUser(user.getId());

        Section13Form form = new Section13Form();
        if (tenancyId != null) {
            form.setTenancyId(tenancyId);
            tenancyService.getTenancyById(tenancyId, user.getId()).ifPresent(t -> {
                form.setProposedRent(t.getMonthlyRent());
            });
        }
        form.setStartingDate(section13Calculator.calculateEarliestStartingDate(LocalDate.now()));

        model.addAttribute("section13Form", form);
        model.addAttribute("tenancies", activeTenancies);
        return "notice/section13";
    }

    @PostMapping("/section13")
    public String generateSection13(@Valid @ModelAttribute("section13Form") Section13Form form,
                                    BindingResult result,
                                    Authentication auth,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        User user = getUser(auth);

        if (result.hasErrors()) {
            model.addAttribute("tenancies", tenancyService.getActiveTenanciesForUser(user.getId()));
            return "notice/section13";
        }

        try {
            LegalNotice notice = legalNoticeService.createSection13Notice(
                    form.getTenancyId(),
                    user.getId(),
                    form.getProposedRent(),
                    form.getStartingDate()
            );

            redirectAttributes.addFlashAttribute("success", "Section 13 Notice generated successfully!");
            return "redirect:/notices/" + notice.getId() + "/print";
        } catch (IllegalArgumentException e) {
            result.reject("error.eligibility", e.getMessage());
            model.addAttribute("tenancies", tenancyService.getActiveTenanciesForUser(user.getId()));
            return "notice/section13";
        }
    }

    @GetMapping("/section8")
    public String section8Form(@RequestParam(required = false) UUID tenancyId, Authentication auth, Model model) {
        User user = getUser(auth);
        List<Tenancy> activeTenancies = tenancyService.getActiveTenanciesForUser(user.getId());

        Section8Form form = new Section8Form();
        if (tenancyId != null) {
            form.setTenancyId(tenancyId);
        }
        form.setStartingDate(LocalDate.now().plusMonths(2)); // Default 2 months notice

        model.addAttribute("section8Form", form);
        model.addAttribute("tenancies", activeTenancies);
        return "notice/section8";
    }

    @PostMapping("/section8")
    public String generateSection8(@Valid @ModelAttribute("section8Form") Section8Form form,
                                   BindingResult result,
                                   Authentication auth,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        User user = getUser(auth);

        if (result.hasErrors()) {
            model.addAttribute("tenancies", tenancyService.getActiveTenanciesForUser(user.getId()));
            return "notice/section8";
        }

        LegalNotice notice = legalNoticeService.createSection8Notice(
                form.getTenancyId(),
                user.getId(),
                form.getStartingDate(),
                form.getGrounds()
        );

        redirectAttributes.addFlashAttribute("success", "Section 8 Notice generated successfully!");
        return "redirect:/notices/" + notice.getId() + "/print";
    }

    @GetMapping("/{id}/print")
    public String printNotice(@PathVariable UUID id, Authentication auth, Model model) {
        User user = getUser(auth);
        LegalNotice notice = legalNoticeService.getNoticeById(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notice not found or access denied"));

        BigDecimal percentageIncrease = section13Calculator.calculatePercentageIncrease(
                notice.getCurrentRent(),
                notice.getProposedRent()
        );

        model.addAttribute("notice", notice);
        model.addAttribute("landlord", user);
        model.addAttribute("percentageIncrease", percentageIncrease);
        return "notice/print";
    }

    @PostMapping("/{id}/serve")
    public String markServed(@PathVariable UUID id,
                             @RequestParam(defaultValue = "FIRST_CLASS_POST") String servedMethod,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User user = getUser(auth);
        try {
            legalNoticeService.markNoticeServed(id, user.getId(), servedMethod);
            redirectAttributes.addFlashAttribute("success", "Notice marked as served.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notices";
    }

    private User getUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class Section13Form {
        @NotNull(message = "Tenancy is required")
        private UUID tenancyId;

        @NotNull(message = "Proposed new rent is required")
        private BigDecimal proposedRent;

        @NotNull(message = "Starting date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startingDate;

        public UUID getTenancyId() { return tenancyId; }
        public void setTenancyId(UUID tenancyId) { this.tenancyId = tenancyId; }

        public BigDecimal getProposedRent() { return proposedRent; }
        public void setProposedRent(BigDecimal proposedRent) { this.proposedRent = proposedRent; }

        public LocalDate getStartingDate() { return startingDate; }
        public void setStartingDate(LocalDate startingDate) { this.startingDate = startingDate; }
    }

    public static class Section8Form {
        @NotNull(message = "Tenancy is required")
        private UUID tenancyId;

        @NotNull(message = "Starting date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate startingDate;

        private String grounds = "Ground 8 (Rent Arrears - 3 Months Mandatory)";

        public UUID getTenancyId() { return tenancyId; }
        public void setTenancyId(UUID tenancyId) { this.tenancyId = tenancyId; }

        public LocalDate getStartingDate() { return startingDate; }
        public void setStartingDate(LocalDate startingDate) { this.startingDate = startingDate; }

        public String getGrounds() { return grounds; }
        public void setGrounds(String grounds) { this.grounds = grounds; }
    }
}
