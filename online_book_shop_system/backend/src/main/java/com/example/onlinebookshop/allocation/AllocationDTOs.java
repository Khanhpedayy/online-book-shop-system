package com.example.onlinebookshop.allocation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class AllocationSettingsDTO {
    private String fifoBy; // LOT | COPY
    private int reservationTtlMin; // minutes
    private String conditionPriority; // NEWEST_FIRST | CHEAPEST_FIRST
    private boolean allowStaffOverride;
}
