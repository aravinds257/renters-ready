package com.rentersready.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyType {
    FLAT("Flat / Apartment"),
    TERRACED("Terraced House"),
    SEMI_DETACHED("Semi-Detached House"),
    DETACHED("Detached House"),
    HMO("HMO (House in Multiple Occupation)");

    private final String displayLabel;
}
