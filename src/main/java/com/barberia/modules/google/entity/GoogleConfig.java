package com.barberia.modules.google.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "google_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleConfig {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", length = 255)
    private String username;

    /** AES-256-GCM, nunca en texto plano. Ver {@link com.barberia.common.CryptoService}. */
    @Column(name = "app_password_encrypted", length = 500)
    private String appPasswordEncrypted;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    /** Cacheado tras el primer descubrimiento CardDAV exitoso, para no repetir el handshake en cada sync. */
    @Column(name = "addressbook_home_url", length = 500)
    private String addressbookHomeUrl;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
