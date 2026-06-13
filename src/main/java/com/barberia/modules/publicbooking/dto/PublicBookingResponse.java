package com.barberia.modules.publicbooking.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PublicBookingResponse {

    private final Integer appointmentId;
    private final String appointmentCode;
    private final String status;
    private final String customerName;
    private final String workerName;
    private final List<ServiceItem> services;
    private final String appointmentDate;
    private final String startTime;
    private final String endTime;
    private final BigDecimal estimatedTotal;
    private final String message;

    @Getter
    @Builder
    public static class ServiceItem {
        private final Integer id;
        private final String name;
        private final BigDecimal price;
        private final Integer durationMinutes;
    }
}
