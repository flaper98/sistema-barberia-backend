package com.barberia.modules.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(AuditAction action, String actorEmail,
                        String entityType, String entityId, String detail) {
        publisher.publishEvent(
                new AuditEvent(this, action, actorEmail, entityType, entityId, detail, resolveIp()));
    }

    private String resolveIp() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "SYSTEM";
            var req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return req.getRemoteAddr();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
