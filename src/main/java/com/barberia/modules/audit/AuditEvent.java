package com.barberia.modules.audit;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
public class AuditEvent extends ApplicationEvent {

    private final AuditAction action;
    private final String actorEmail;
    private final String entityType;
    private final String entityId;
    private final String detail;
    private final String ipAddress;
    private final Instant occurredAt;

    public AuditEvent(Object source, AuditAction action, String actorEmail,
                      String entityType, String entityId, String detail, String ipAddress) {
        super(source);
        this.action      = action;
        this.actorEmail  = actorEmail;
        this.entityType  = entityType;
        this.entityId    = entityId;
        this.detail      = detail;
        this.ipAddress   = ipAddress;
        this.occurredAt  = Instant.now();
    }
}
