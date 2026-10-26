-- =====================================================================
-- MINIMARKET "EL SOL" - Script de creacion de base de datos
-- Curso Integrador I - Desarrollo de Software
-- Motor: MySQL 8 | Base: elsol_db | Charset: utf8mb4
-- Uso: mysql -u root -p < elsol_db.sql
-- =====================================================================
CREATE DATABASE IF NOT EXISTS elsol_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE elsol_db;

-- ---------------------------------------------------------------------
-- Tabla: usuario (accesos al sistema, clave en SHA-256, nunca texto plano)
-- Los 2 PERFILES del sistema se guardan en la columna "rol": ADMIN / CAJERO
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(50) NOT NULL UNIQUE,
  clave VARCHAR(64) NOT NULL,
  nombre VARCHAR(100),
  rol VARCHAR(20)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: categoria (categorias del catalogo)
-- activo: 1 = vigente, 0 = eliminado logicamente
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  descripcion VARCHAR(100),
  activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: producto (catalogo del minimarket)
-- codigo: UNICO; el cajero lo digita en la venta (emula el scanner).
-- costo: precio de compra; permite calcular la utilidad en reportes.
-- stock_minimo: umbral por tipo; si stock <= stock_minimo => "Stock bajo".
-- activo: 1 = vigente, 0 = ELIMINADO LOGICAMENTE (no se borra la fila).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(30) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  principio_activo VARCHAR(100) NOT NULL DEFAULT '',
  precio DECIMAL(10,2) NOT NULL,
  costo DECIMAL(10,2) NOT NULL DEFAULT 0,
  stock INT NOT NULL,
  stock_minimo INT NOT NULL DEFAULT 5,
  id_categoria INT,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: venta (cabecera de cada transaccion del punto de venta)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
  id_venta INT AUTO_INCREMENT PRIMARY KEY,
  fecha DATETIME NOT NULL,
  hora_inicio DATETIME NOT NULL,
  hora_fin DATETIME NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado VARCHAR(12) NOT NULL DEFAULT 'VIGENTE',
  dni_cliente VARCHAR(12),
  nombre_cliente VARCHAR(100),
  metodo_pago VARCHAR(20),
  id_usuario INT,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: detalle_venta (lineas de cada venta)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_venta (
  id_detalle INT AUTO_INCREMENT PRIMARY KEY,
  id_venta INT NOT NULL,
  id_producto INT NOT NULL,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

-- =====================================================================
-- Datos iniciales
-- =====================================================================
-- Usuarios (clave "1234" en SHA-256):
-- 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4
INSERT INTO usuario (usuario, clave, nombre, rol)
SELECT 'admin', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Administrador El Sol', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario = 'admin');
INSERT INTO usuario (usuario, clave, nombre, rol)
SELECT 'cajero', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Personal El Sol', 'CAJERO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario = 'cajero');

-- Categorias farmaceuticas
INSERT INTO categoria (descripcion)
SELECT 'Analgesicos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Analgesicos');
INSERT INTO categoria (descripcion)
SELECT 'Antibioticos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Antibioticos');
INSERT INTO categoria (descripcion)
SELECT 'Antiinflamatorios' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Antiinflamatorios');
INSERT INTO categoria (descripcion)
SELECT 'Antihistaminicos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Antihistaminicos');
INSERT INTO categoria (descripcion)
SELECT 'Vitaminas y suplementos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Vitaminas y suplementos');
INSERT INTO categoria (descripcion)
SELECT 'Cuidado personal' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Cuidado personal');

-- Productos (codigo, nombre comercial, principio activo, precio, stock, minimo, categoria)
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F001', 'Panadol 500mg x20 tab', 'Paracetamol', 12.50, 80, 10, id_categoria FROM categoria WHERE descripcion = 'Analgesicos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F001');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F002', 'Apronax 550mg x10 tab', 'Naproxeno', 18.90, 50, 8, id_categoria FROM categoria WHERE descripcion = 'Antiinflamatorios'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F002');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F003', 'Ibuprofeno 400mg x10 tab', 'Ibuprofeno', 9.50, 60, 8, id_categoria FROM categoria WHERE descripcion = 'Antiinflamatorios'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F003');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F004', 'Amoxicilina 500mg x12 cap', 'Amoxicilina', 22.00, 40, 6, id_categoria FROM categoria WHERE descripcion = 'Antibioticos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F004');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F005', 'Azitromicina 500mg x3 tab', 'Azitromicina', 25.50, 30, 5, id_categoria FROM categoria WHERE descripcion = 'Antibioticos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F005');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F006', 'Cetirizina 10mg x10 tab', 'Cetirizina', 8.00, 70, 10, id_categoria FROM categoria WHERE descripcion = 'Antihistaminicos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F006');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F007', 'Loratadina 10mg x10 tab', 'Loratadina', 7.50, 65, 10, id_categoria FROM categoria WHERE descripcion = 'Antihistaminicos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F007');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F008', 'Omeprazol 20mg x14 cap', 'Omeprazol', 11.00, 55, 8, id_categoria FROM categoria WHERE descripcion = 'Analgesicos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F008');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F009', 'Vitamina C 1g x10 eferv', 'Acido ascorbico', 15.00, 45, 6, id_categoria FROM categoria WHERE descripcion = 'Vitaminas y suplementos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F009');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F010', 'Alcohol 70% 1L', 'Alcohol etilico', 9.90, 100, 12, id_categoria FROM categoria WHERE descripcion = 'Cuidado personal'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F010');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F011', 'Mascarilla quirurgica x50', 'No aplica', 19.90, 120, 15, id_categoria FROM categoria WHERE descripcion = 'Cuidado personal'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F011');
INSERT INTO producto (codigo, nombre, principio_activo, precio, stock, stock_minimo, id_categoria)
SELECT 'F012', 'Paracetamol jarabe 120mg/5ml', 'Paracetamol', 13.50, 35, 5, id_categoria FROM categoria WHERE descripcion = 'Analgesicos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'F012');

-- =====================================================================
-- Costo de compra de los productos de ejemplo (80% del precio de venta).
-- Se usa para calcular la UTILIDAD en el reporte ingresos/utilidad/stock.
-- =====================================================================
UPDATE producto SET costo = ROUND(precio * 0.80, 2);

-- =====================================================================
-- MIGRACION para bases de datos creadas con la version anterior.
-- Agrega las columnas costo y activo solo si no existen (MySQL no
-- soporta ADD COLUMN IF NOT EXISTS, por eso se usa un procedimiento).
-- =====================================================================
DROP PROCEDURE IF EXISTS migrar_columnas;
DELIMITER //
CREATE PROCEDURE migrar_columnas()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'elsol_db' AND table_name = 'producto' AND column_name = 'costo') THEN
    ALTER TABLE producto ADD COLUMN costo DECIMAL(10,2) NOT NULL DEFAULT 0;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'elsol_db' AND table_name = 'producto' AND column_name = 'activo') THEN
    ALTER TABLE producto ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema = 'elsol_db' AND table_name = 'categoria' AND column_name = 'activo') THEN
    ALTER TABLE categoria ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1;
  END IF;
END //
DELIMITER ;
CALL migrar_columnas();
DROP PROCEDURE migrar_columnas;


