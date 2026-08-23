package com.barberia.modules.whatsapp.repository;

import com.barberia.modules.whatsapp.entity.WhatsAppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsAppConfigRepository extends JpaRepository<WhatsAppConfig, Integer> {
}
