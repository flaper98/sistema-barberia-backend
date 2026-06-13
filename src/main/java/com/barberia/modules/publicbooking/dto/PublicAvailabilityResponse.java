package com.barberia.modules.publicbooking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PublicAvailabilityResponse {

    private final String date;
    private final Integer serviceDurationMinutes;
    private final List<WorkerSlots> availableSlots;

    @Getter
    @Builder
    public static class WorkerSlots {
        private final Integer workerId;
        private final String workerName;
        private final List<TimeSlot> slots;
    }

    @Getter
    @Builder
    public static class TimeSlot {
        private final String startTime;
        private final String endTime;
    }
}
