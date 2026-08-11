package com.rentersready.controller;

import com.rentersready.model.AuditLog;
import com.rentersready.model.Property;
import com.rentersready.model.User;
import com.rentersready.service.AuditLogService;
import com.rentersready.service.PropertyService;
import com.rentersready.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final PropertyService propertyService;
    private final UserService userService;

    public AuditLogController(AuditLogService auditLogService,
                              PropertyService propertyService,
                              UserService userService) {
        this.auditLogService = auditLogService;
        this.propertyService = propertyService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = getUser(auth);
        List<AuditLog> logs = auditLogService.getAuditLogsForUser(user.getId());
        List<Property> properties = propertyService.getPropertiesForUser(user.getId());

        model.addAttribute("logs", logs);
        model.addAttribute("properties", properties);
        model.addAttribute("logForm", new LogForm());
        return "audit/list";
    }

    @PostMapping("/add")
    public String addLog(@Valid @ModelAttribute("logForm") LogForm form,
                         BindingResult result,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {

        User user = getUser(auth);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Description is required to log an entry.");
            return "redirect:/audit-logs";
        }

        auditLogService.logCommunication(
                user.getId(),
                form.getPropertyId(),
                form.getActionType(),
                form.getDescription()
        );

        redirectAttributes.addFlashAttribute("success", "Communication entry logged in audit trail.");
        return "redirect:/audit-logs";
    }

    private User getUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public static class LogForm {
        private UUID propertyId;
        private String actionType = "TENANT_COMMUNICATION";

        @NotBlank(message = "Description is required")
        private String description;

        public UUID getPropertyId() { return propertyId; }
        public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
