package com.barberia.modules.whatsapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppConfig {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "phone_number_id", length = 50)
    private String phoneNumberId;

    /** AES-256-GCM, nunca en texto plano. Ver {@link com.barberia.common.CryptoService}. */
    @Column(name = "access_token_encrypted", length = 1000)
    private String accessTokenEncrypted;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "language_code", length = 10, nullable = false)
    @Builder.Default
    private String languageCode = "es";

    @Column(name = "graph_api_version", length = 10, nullable = false)
    @Builder.Default
    private String graphApiVersion = "v21.0";

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
