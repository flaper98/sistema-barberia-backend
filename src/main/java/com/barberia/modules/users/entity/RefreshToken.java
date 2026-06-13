package com.barberia.modules.users.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Column(name = "expira_at", nullable = false)
    private LocalDateTime expiraAt;

    @Column(name = "revocado", nullable = false)
    @Builder.Default
    private Boolean revocado = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiraAt);
    }

    public boolean isValid() {
        return !revocado && !isExpired();
    }
}
