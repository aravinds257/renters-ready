package com.rentersready.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CheckStatus {
    VALID("Valid", "success"),
    EXPIRING_SOON("Expiring Soon", "warning"),
    EXPIRED("Expired", "danger"),
    MISSING("Missing", "danger");

    private final String displayLabel;
    private final String cssClass;
}
