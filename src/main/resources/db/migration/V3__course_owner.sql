-- =====================================================================
-- V3: Docente propietario de cada módulo educativo.
--
-- Permite que un docente vea y gestione solo sus cursos, mientras el
-- administrador conserva la visión global. El campo es nullable para no
-- romper el contenido histórico creado antes de esta versión.
-- =====================================================================

ALTER TABLE modulo ADD COLUMN IF NOT EXISTS iddocente integer;

CREATE INDEX IF NOT EXISTS idx_modulo_docente ON modulo (iddocente);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_modulo_docente'
    ) THEN
        ALTER TABLE modulo ADD CONSTRAINT fk_modulo_docente
            FOREIGN KEY (iddocente) REFERENCES usuario (idusuario);
    END IF;
END $$;
