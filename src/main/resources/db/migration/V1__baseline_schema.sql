-- =====================================================================
-- V1: Esquema base de Mundo Manitas
--
-- Idempotente: puede ejecutarse sobre una base existente creada por
-- Hibernate sin destruir información.
--
-- Nota: los identificadores se escriben sin comillas y en minúsculas
-- porque PostgreSQL pliega los identificadores no citados a minúsculas,
-- que es exactamente lo que genera Hibernate con
-- PhysicalNamingStrategyStandardImpl (idUsuario -> idusuario).
-- =====================================================================

CREATE TABLE IF NOT EXISTS rol (
    idrol        serial       PRIMARY KEY,
    codigo       varchar(40)  NOT NULL,
    nombre       varchar(80)  NOT NULL,
    descripcion  varchar(180),
    activo       boolean      NOT NULL DEFAULT true,
    CONSTRAINT uk_rol_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS modulo_sistema (
    idmodulosistema serial      PRIMARY KEY,
    codigo          varchar(50) NOT NULL,
    nombre          varchar(80) NOT NULL,
    ruta            varchar(120) NOT NULL,
    orden           integer     NOT NULL,
    activo          boolean     NOT NULL DEFAULT true,
    CONSTRAINT uk_modulo_sistema_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS rol_modulo (
    idrol            integer NOT NULL,
    idmodulosistema  integer NOT NULL,
    CONSTRAINT rol_modulo_pkey PRIMARY KEY (idrol, idmodulosistema),
    CONSTRAINT fk_rol_modulo_rol FOREIGN KEY (idrol) REFERENCES rol (idrol),
    CONSTRAINT fk_rol_modulo_modulo FOREIGN KEY (idmodulosistema) REFERENCES modulo_sistema (idmodulosistema)
);

CREATE TABLE IF NOT EXISTS usuario (
    idusuario      serial       PRIMARY KEY,
    nombre         varchar(100) NOT NULL,
    email          varchar(100) NOT NULL,
    contrasena     varchar(255) NOT NULL,
    idrol          integer      NOT NULL,
    activo         boolean      NOT NULL DEFAULT true,
    fecharegistro  timestamp    NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (idrol) REFERENCES rol (idrol)
);

CREATE TABLE IF NOT EXISTS estudiante (
    idestudiante  integer     PRIMARY KEY,
    grado         varchar(20),
    edad          integer,
    nivelestudio  varchar(50),
    CONSTRAINT fk_estudiante_usuario FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario)
);

CREATE TABLE IF NOT EXISTS modulo (
    idmodulo     serial       PRIMARY KEY,
    titulo       varchar(100) NOT NULL,
    descripcion  text,
    nivel        varchar(255) NOT NULL,
    orden        integer      NOT NULL
);

CREATE TABLE IF NOT EXISTS video (
    idvideo   serial       PRIMARY KEY,
    idmodulo  integer      NOT NULL,
    titulo    varchar(100) NOT NULL,
    url       varchar(255) NOT NULL,
    duracion  integer,
    orden     integer      NOT NULL,
    CONSTRAINT fk_video_modulo FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo)
);

CREATE TABLE IF NOT EXISTS ejercicio (
    idejercicio        serial       PRIMARY KEY,
    idmodulo           integer      NOT NULL,
    titulo             varchar(100) NOT NULL,
    tipo               varchar(255) NOT NULL,
    contenido          text,
    respuestacorrecta  text,
    CONSTRAINT fk_ejercicio_modulo FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo)
);

CREATE TABLE IF NOT EXISTS progreso (
    idprogreso       serial        PRIMARY KEY,
    idestudiante     integer       NOT NULL,
    idmodulo         integer       NOT NULL,
    fechainicio      timestamp     NOT NULL,
    fechacompletado  timestamp,
    calificacion     numeric(5,2),
    porcentaje       numeric(5,2),
    CONSTRAINT fk_progreso_estudiante FOREIGN KEY (idestudiante) REFERENCES usuario (idusuario),
    CONSTRAINT fk_progreso_modulo FOREIGN KEY (idmodulo) REFERENCES modulo (idmodulo)
);

CREATE INDEX IF NOT EXISTS idx_video_modulo ON video (idmodulo);
CREATE INDEX IF NOT EXISTS idx_ejercicio_modulo ON ejercicio (idmodulo);
CREATE INDEX IF NOT EXISTS idx_progreso_estudiante ON progreso (idestudiante);
