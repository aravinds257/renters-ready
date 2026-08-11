package com.rentersready.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeStatus {
    DRAFT("Draft", "secondary"),
    SERVED("Served to Tenant", "primary"),
    ACCEPTED("Accepted", "success"),
    DISPUTED("Disputed", "danger");

    private final String displayLabel;
    private final String cssClass;
}
