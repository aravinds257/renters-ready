package com.rentersready.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeType {
    SECTION_13("Section 13 Notice (Rent Increase)"),
    SECTION_8("Section 8 Notice (Possession)");

    private final String displayLabel;
}
