-- Migración para suscripciones recurrentes Culqi en PeruTalent
-- Ejecutar en Supabase SQL Editor antes de probar el flujo recurrente.

ALTER TABLE plan_suscripcion
ADD COLUMN IF NOT EXISTS culqi_plan_id VARCHAR(150);

ALTER TABLE suscripcion_usuario
ADD COLUMN IF NOT EXISTS renovacion_automatica BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS fecha_ultimo_cobro TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS fecha_proximo_cobro DATE NULL,
ADD COLUMN IF NOT EXISTS fecha_cancelacion TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS motivo_cancelacion VARCHAR(500) NULL;

ALTER TABLE pago_suscripcion
ADD COLUMN IF NOT EXISTS culqi_subscription_id VARCHAR(150);

CREATE TABLE IF NOT EXISTS culqi_evento (
    id_culqi_evento BIGSERIAL PRIMARY KEY,
    culqi_event_id VARCHAR(150) NOT NULL UNIQUE,
    tipo_evento VARCHAR(100) NOT NULL,
    id_pago BIGINT NULL,
    id_suscripcion BIGINT NULL,
    procesado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_recepcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload TEXT NOT NULL,
    CONSTRAINT fk_culqi_evento_pago
        FOREIGN KEY (id_pago)
        REFERENCES pago_suscripcion(id_pago),
    CONSTRAINT fk_culqi_evento_suscripcion
        FOREIGN KEY (id_suscripcion)
        REFERENCES suscripcion_usuario(id_suscripcion)
);

CREATE INDEX IF NOT EXISTS idx_suscripcion_usuario_culqi_subscription
ON suscripcion_usuario(culqi_subscription_id);

CREATE INDEX IF NOT EXISTS idx_suscripcion_usuario_estado_plan
ON suscripcion_usuario(id_usuario, estado_suscripcion, id_plan);

CREATE INDEX IF NOT EXISTS idx_pago_suscripcion_culqi_subscription
ON pago_suscripcion(culqi_subscription_id);

CREATE INDEX IF NOT EXISTS idx_culqi_evento_tipo
ON culqi_evento(tipo_evento);

-- IMPORTANTE:
-- Reemplaza pln_test_xxxxxxxxxxxxxxxxx por el ID real del plan mensual creado en CulqiPanel.
-- También puedes no ejecutar este UPDATE y configurar CULQI_PREMIUM_PLAN_ID en Render/Docker.
--
-- UPDATE plan_suscripcion
-- SET culqi_plan_id = 'pln_test_xxxxxxxxxxxxxxxxx'
-- WHERE UPPER(nombre_plan) = 'PREMIUM';
