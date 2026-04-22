-- ============================================================
--  Sistema de Gestion de Recolectores — Schema SQL
--  Motor: MySQL 8.x
--  Ejecutar este script ANTES de arrancar la aplicacion.
--  Hibernate (hbm2ddl = update) también puede crear las tablas,
--  pero este script garantiza constraints y datos iniciales.
-- ============================================================

CREATE DATABASE IF NOT EXISTS recolectores_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE recolectores_db;

-- ── Tabla: LOTES ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS LOTES (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    codigo      VARCHAR(10)   NOT NULL UNIQUE COMMENT 'Ej: A1, B2',
    descripcion VARCHAR(200),
    activo      TINYINT(1)    NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: RECOLECTORES ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS RECOLECTORES (
    id      BIGINT       PRIMARY KEY AUTO_INCREMENT,
    nombre  VARCHAR(100) NOT NULL,
    cedula  VARCHAR(20)  NOT NULL UNIQUE,
    lote_id BIGINT,
    activo  TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT fk_recolector_lote FOREIGN KEY (lote_id) REFERENCES LOTES(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tabla: PESAJES ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS PESAJES (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    recolector_id BIGINT        NOT NULL,
    lote_id       BIGINT        NOT NULL,
    fecha         DATE          NOT NULL,
    kilos         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_pesaje_recolector FOREIGN KEY (recolector_id) REFERENCES RECOLECTORES(id),
    CONSTRAINT fk_pesaje_lote       FOREIGN KEY (lote_id)       REFERENCES LOTES(id),
    CONSTRAINT chk_kilos_positivos  CHECK (kilos > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
--  PATCH: Módulo de Login
--  Ejecutar UNA sola vez sobre la base de datos existente.
-- ============================================================
USE recolectores_db;

CREATE TABLE IF NOT EXISTS USUARIOS (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(64)  NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    activo   BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

-- ── Índices para rendimiento en consultas de reportes ─────────
CREATE INDEX IF NOT EXISTS idx_pesajes_fecha
    ON PESAJES(fecha);
CREATE INDEX IF NOT EXISTS idx_pesajes_recolector
    ON PESAJES(recolector_id);
CREATE INDEX IF NOT EXISTS idx_pesajes_lote
    ON PESAJES(lote_id);

-- ── Datos de ejemplo (opcional, borrar si no se necesitan) ────
INSERT IGNORE INTO LOTES (codigo, descripcion) VALUES
    ('A1', 'Lote norte - Sector 1'),
    ('A2', 'Lote norte - Sector 2'),
    ('B1', 'Lote sur - Sector 1');



-- Contraseña por defecto: Admin1234
-- SHA-256("Admin1234") = 0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90
INSERT IGNORE INTO USUARIOS (username, password, nombre, activo)
VALUES (
    'admin',
    '0a041b9462caa4a31bac3567e0b6e6fd9100787db2ab433d96f6d178cabfce90',
    'Administrador',
    TRUE
);


--Se actualiza el hash
UPDATE USUARIOS 
SET password = '60fe74406e7f353ed979f350f2fbb6a2e8690a5fa7d1b0c32983d1d8b3f95f67'
WHERE username = 'admin';
--PARA MODIFICAR LA RELACIÓN DE USUARIO A PESAJE PARA SABER
--QUIEN HIZO EL PESAJE
-- ============================================================
--  PATCH: Agregar usuario_id a PESAJES
--  Ejecutar UNA sola vez.
-- ============================================================
USE recolectores_db;

ALTER TABLE PESAJES
  ADD COLUMN usuario_id BIGINT NOT NULL REFERENCES USUARIOS(id);

CREATE INDEX idx_pesajes_usuario ON PESAJES(usuario_id);