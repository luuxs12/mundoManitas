-- =====================================================================
-- V2: Seguridad de cuentas, inscripciones, progreso automático,
--     intentos de ejercicios, integridad referencial e índices.
--
-- Idempotente para bases ya existentes. Los identificadores van sin
-- comillas: PostgreSQL los pliega a minúsculas, igual que Hibernate.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Seguridad de la cuenta
-- ---------------------------------------------------------------------
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS intentosfallidos integer NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS bloqueadohasta timestamp;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS tokenrecuperacion varchar(120);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS tokenrecuperacionexpira timestamp;

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_token_recuperacion
    ON usuario (tokenrecuperacion)
    WHERE tokenrecuperacion IS NOT NULL;

-- ---------------------------------------------------------------------
-- Inscripciones a módulos
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inscripcion (
    idinscripcion     serial      PRIMARY KEY,
    idestudiante      integer     NOT NULL,
    idmodulo          integer     NOT NULL,
    fechainscripcion  timestamp   NOT NULL DEFAULT now(),
    estado            varchar(20) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT uk_inscripcion_estudiante_modulo UNIQUE (idestudiante, idmodulo)
);

-- ---------------------------------------------------------------------
-- Progreso por video
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS video_progreso (
    idvideoprogreso  serial    PRIMARY KEY,
    idestudiante     integer   NOT NULL,
    idvideo          integer   NOT NULL,
    completado       boolean   NOT NULL DEFAULT false,
    fechacompletado  timestamp,
    CONSTRAINT uk_video_progreso_estudiante_video UNIQUE (idestudiante, idvideo)
);

-- ---------------------------------------------------------------------
-- Intentos de ejercicios
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS intento_ejercicio (
    idintento     serial       PRIMARY KEY,
    idestudiante  integer      NOT NULL,
    idejercicio   integer      NOT NULL,
    respuesta     text,
    correcto      boolean      NOT NULL DEFAULT false,
    puntaje       numeric(5,2),
    fechaintento  timestamp    NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- Restricción de unicidad del progreso por estudiante/módulo
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS uk_progreso_estudiante_modulo
    ON progreso (idestudiante, idmodulo);

-- ---------------------------------------------------------------------
-- Llaves foráneas faltantes (idempotentes)
-- ---------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_progreso_estudiante') THEN
        ALTER TABLE progreso ADD CONSTRAINT fk_progreso_estudiante
            FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_progreso_modulo') THEN
        ALTER TABLE progreso ADD CONSTRAINT fk_progreso_modulo
            FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_video_modulo') THEN
        ALTER TABLE video ADD CONSTRAINT fk_video_modulo
            FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_ejercicio_modulo') THEN
        ALTER TABLE ejercicio ADD CONSTRAINT fk_ejercicio_modulo
            FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_estudiante_usuario') THEN
        ALTER TABLE estudiante ADD CONSTRAINT fk_estudiante_usuario
            FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inscripcion_estudiante') THEN
        ALTER TABLE inscripcion ADD CONSTRAINT fk_inscripcion_estudiante
            FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_inscripcion_modulo') THEN
        ALTER TABLE inscripcion ADD CONSTRAINT fk_inscripcion_modulo
            FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_video_progreso_estudiante') THEN
        ALTER TABLE video_progreso ADD CONSTRAINT fk_video_progreso_estudiante
            FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_video_progreso_video') THEN
        ALTER TABLE video_progreso ADD CONSTRAINT fk_video_progreso_video
            FOREIGN KEY (idvideo) REFERENCES video (idvideo);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_intento_ejercicio_estudiante') THEN
        ALTER TABLE intento_ejercicio ADD CONSTRAINT fk_intento_ejercicio_estudiante
            FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_intento_ejercicio_ejercicio') THEN
        ALTER TABLE intento_ejercicio ADD CONSTRAINT fk_intento_ejercicio_ejercicio
            FOREIGN KEY (idejercicio) REFERENCES ejercicio (idejercicio);
    END IF;
END $$;

-- ---------------------------------------------------------------------
-- Índices de rendimiento
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_inscripcion_estudiante ON inscripcion (idestudiante);
CREATE INDEX IF NOT EXISTS idx_inscripcion_modulo ON inscripcion (idmodulo);
CREATE INDEX IF NOT EXISTS idx_video_progreso_estudiante ON video_progreso (idestudiante);
CREATE INDEX IF NOT EXISTS idx_video_progreso_video ON video_progreso (idvideo);
CREATE INDEX IF NOT EXISTS idx_intento_ejercicio_estudiante ON intento_ejercicio (idestudiante);
CREATE INDEX IF NOT EXISTS idx_intento_ejercicio_ejercicio ON intento_ejercicio (idejercicio);
CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuario (idrol);
CREATE INDEX IF NOT EXISTS idx_modulo_orden ON modulo (orden);
CREATE INDEX IF NOT EXISTS idx_usuario_activo ON usuario (activo);
