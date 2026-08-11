package com.rentersready.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CheckType {
    GAS_SAFETY("Gas Safety Certificate (CP12)", true),
    EPC("Energy Performance Certificate (EPC)", true),
    EICR("Electrical Safety Certificate (EICR)", true),
    HOW_TO_RENT("How to Rent Guide", false),
    DEPOSIT_INFO("Deposit Prescribed Information", false);

    private final String displayLabel;
    private final boolean requiresExpiryDate;
}
