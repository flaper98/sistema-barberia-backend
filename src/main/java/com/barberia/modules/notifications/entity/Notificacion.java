package com.barberia.modules.notifications.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 255)
    private String mensaje;

    @Column(name = "entidad_tipo", length = 30)
    private String entidadTipo;

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(nullable = false)
    @Builder.Default
    private boolean leida = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;
}
