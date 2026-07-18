package com.barberia.modules.barbershop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_barberia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionBarberia {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "horario", length = 150)
    private String horario;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "mensaje_bienvenida_whatsapp", length = 500)
    private String mensajeBienvenidaWhatsapp;

    @Column(name = "mensaje_recordatorio_whatsapp", length = 500)
    private String mensajeRecordatorioWhatsapp;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
