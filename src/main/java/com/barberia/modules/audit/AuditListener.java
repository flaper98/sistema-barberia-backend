package com.barberia.modules.audit;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Escucha AuditEvent y lo escribe en el log estructurado.
 * Se puede extender para persistir en tabla si el DBA la crea luego.
 * Usa @Async para no bloquear el hilo HTTP.
 */
@Slf4j
@Component
public class AuditListener {

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        try {
            MDC.put("audit.action",     event.getAction().name());
            MDC.put("audit.actor",      event.getActorEmail());
            MDC.put("audit.entity",     event.getEntityType());
            MDC.put("audit.entityId",   event.getEntityId());
            MDC.put("audit.ip",         event.getIpAddress());
            MDC.put("audit.occurredAt", event.getOccurredAt().toString());

            log.info("AUDIT | action={} actor={} entity={} entityId={} detail={} ip={}",
                    event.getAction(),
                    event.getActorEmail(),
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getDetail(),
                    event.getIpAddress());
        } finally {
            MDC.remove("audit.action");
            MDC.remove("audit.actor");
            MDC.remove("audit.entity");
            MDC.remove("audit.entityId");
            MDC.remove("audit.ip");
            MDC.remove("audit.occurredAt");
        }
    }
}
