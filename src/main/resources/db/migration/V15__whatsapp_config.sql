-- Configuracion de envio automatico de WhatsApp (API oficial de Meta) al
-- registrar un cliente nuevo. Fila unica (id=1), igual que icloud_config.
CREATE TABLE whatsapp_config (
    id                     INT PRIMARY KEY,
    phone_number_id        VARCHAR(50)  NULL,
    access_token_encrypted VARCHAR(1000) NULL,
    template_name          VARCHAR(100) NULL,
    language_code          VARCHAR(10)  NOT NULL DEFAULT 'es',
    graph_api_version      VARCHAR(10)  NOT NULL DEFAULT 'v21.0',
    enabled                BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at             DATETIME(6)  NOT NULL
);
