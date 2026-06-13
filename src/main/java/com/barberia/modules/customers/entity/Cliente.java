package com.barberia.modules.customers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "correo", unique = true, length = 150)
    private String correo;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @Column(name = "cantidad_sellos", nullable = false)
    @Builder.Default
    private Integer cantidadSellos = 0;

    @Column(name = "recompensas_disponibles", nullable = false)
    @Builder.Default
    private Integer recompensasDisponibles = 0;

    @Column(name = "ultima_visita")
    private LocalDate ultimaVisita;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
