-- =====================================================================
-- MINIMARKET "EL SOL" - Script de creacion de base de datos
-- Curso Integrador I - Desarrollo de Software
-- Motor: MySQL 8 | Base: elsol_db | Charset: utf8mb4
--
-- IMPORTANTE: este script debe ejecutarse como ROOT porque crea la base
-- de datos y el usuario de la aplicacion:
--   mysql -u root -p < elsol_db.sql
--
-- Para partir de un ambiente limpio (recomendado para la demo):
--   mysql -u root -p  ->  DROP DATABASE IF EXISTS elsol_db;
--   y luego ejecutar este script.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS elsol_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE elsol_db;

-- ---------------------------------------------------------------------
-- Tabla: usuario (accesos al sistema, clave en SHA-256, nunca texto plano)
-- Los 2 PERFILES del sistema se guardan en la columna "rol": ADMIN / CAJERO
-- El CHECK impide guardar un rol distinto de los dos perfiles existentes.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(50) NOT NULL UNIQUE,
  clave VARCHAR(64) NOT NULL,
  nombre VARCHAR(100),
  rol VARCHAR(20) NOT NULL DEFAULT 'CAJERO' CHECK (rol IN ('ADMIN','CAJERO'))
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
  descripcion VARCHAR(100) NOT NULL DEFAULT '',
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
-- MIGRACION para bases de datos creadas con la version anterior (botica):
--  1) renombra la columna principio_activo -> descripcion
--  2) agrega las columnas costo/activo solo si no existen
-- MySQL no soporta ADD COLUMN IF NOT EXISTS, por eso se usa un procedimiento.
-- DEBE ejecutarse ANTES de insertar los datos, porque en una base vieja
-- la tabla producto aun no tiene la columna "descripcion".
-- =====================================================================
DROP PROCEDURE IF EXISTS migrar_columnas;
DELIMITER //
CREATE PROCEDURE migrar_columnas()
BEGIN
  -- principio_activo -> descripcion (solo si la version vieja existe)
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema = 'elsol_db' AND table_name = 'producto' AND column_name = 'principio_activo')
     AND NOT EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema = 'elsol_db' AND table_name = 'producto' AND column_name = 'descripcion') THEN
    ALTER TABLE producto CHANGE COLUMN principio_activo descripcion VARCHAR(100) NOT NULL DEFAULT '';
  END IF;
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

-- Nota: si la base venia de la VERSION BOTICA, conviene limpiar los datos
-- viejos antes de demostrar (descomente segun el caso):
--   DELETE FROM producto WHERE codigo LIKE 'F0%';
--   DELETE FROM categoria WHERE descripcion IN ('Analgesicos','Antibioticos',
--     'Antiinflamatorios','Antihistaminicos','Vitaminas y suplementos');
-- (requiere que no existan ventas que referencien a esos productos).

-- =====================================================================
-- Datos iniciales
-- =====================================================================
-- Usuarios de prueba (los 2 PERFILES pedidos en la indicacion).
--
-- POLITICA DE CONTRASENA: minimo 8 caracteres, con al menos una letra
-- minuscula, una mayuscula, un numero y un simbolo especial.
--   Admin#2026  -> SHA-256 de la clave del ADMIN
--   Cajero2026! -> SHA-256 de la clave del CAJERO
-- La clave NUNCA se guarda en texto plano (ver util.HashUtil).
--
-- ADMIN   -> Admin#2026   (hash b5af2beb9b7fd5aa4eb21b61925bd3856793e54e0a39052fbcfaf5b477fd07f3)
-- CAJERO  -> Cajero2026!  (hash 5cb3b5fd026da381d44a175815afdec4a13f6962d6c982cb61613b331ca9f943)
INSERT INTO usuario (usuario, clave, nombre, rol)
SELECT 'admin', 'b5af2beb9b7fd5aa4eb21b61925bd3856793e54e0a39052fbcfaf5b477fd07f3', 'Administrador El Sol', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario = 'admin');
INSERT INTO usuario (usuario, clave, nombre, rol)
SELECT 'cajero', '5cb3b5fd026da381d44a175815afdec4a13f6962d6c982cb61613b331ca9f943', 'Personal El Sol', 'CAJERO'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE usuario = 'cajero');

-- MIGRACION: si la base existia con las claves de prueba viejas ('1234',
-- hash 03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4),
-- se migran a las nuevas claves que cumplen la politica.
UPDATE usuario SET clave = 'b5af2beb9b7fd5aa4eb21b61925bd3856793e54e0a39052fbcfaf5b477fd07f3'
WHERE usuario = 'admin' AND clave = '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4';
UPDATE usuario SET clave = '43b65a2b4d8028ea8efecd9ff82e7c3e86c645fccfdecd0ddad29945e992c192'
WHERE usuario = 'cajero' AND clave = '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4';

-- Categorias del minimarket
INSERT INTO categoria (descripcion)
SELECT 'Abarrotes' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Abarrotes');
INSERT INTO categoria (descripcion)
SELECT 'Bebidas' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Bebidas');
INSERT INTO categoria (descripcion)
SELECT 'Limpieza' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Limpieza');
INSERT INTO categoria (descripcion)
SELECT 'Snacks' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Snacks');
INSERT INTO categoria (descripcion)
SELECT 'Lacteos y huevos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Lacteos y huevos');
INSERT INTO categoria (descripcion)
SELECT 'Cuidado personal' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE descripcion = 'Cuidado personal');

-- Productos (codigo, nombre, descripcion/presentacion, precio, stock, minimo, categoria)
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'A001', 'Arroz extra 1 kg', 'Bolsa de 1 kg', 4.50, 80, 10, id_categoria FROM categoria WHERE descripcion = 'Abarrotes'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'A001');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'A002', 'Aceite vegetal 1 L', 'Botella de 1 L', 9.90, 50, 8, id_categoria FROM categoria WHERE descripcion = 'Abarrotes'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'A002');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'A003', 'Azucar rubia 1 kg', 'Bolsa de 1 kg', 3.80, 60, 10, id_categoria FROM categoria WHERE descripcion = 'Abarrotes'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'A003');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'A004', 'Fideos tallarin 500 g', 'Paquete de 500 g', 2.90, 70, 10, id_categoria FROM categoria WHERE descripcion = 'Abarrotes'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'A004');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'B001', 'Gaseosa cola 1.5 L', 'Botella de 1.5 L', 6.50, 90, 12, id_categoria FROM categoria WHERE descripcion = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'B001');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'B002', 'Agua mineral 625 ml', 'Botella de 625 ml', 1.50, 120, 20, id_categoria FROM categoria WHERE descripcion = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'B002');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'B003', 'Jugo de naranja 1 L', 'Caja de 1 L', 7.90, 40, 8, id_categoria FROM categoria WHERE descripcion = 'Bebidas'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'B003');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'L001', 'Detergente 800 g', 'Bolsa de 800 g', 8.50, 45, 8, id_categoria FROM categoria WHERE descripcion = 'Limpieza'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'L001');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'L002', 'Laina 1 L', 'Botella de 1 L', 3.20, 60, 10, id_categoria FROM categoria WHERE descripcion = 'Limpieza'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'L002');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'S001', 'Papas fritas 45 g', 'Bolsa de 45 g', 1.80, 100, 15, id_categoria FROM categoria WHERE descripcion = 'Snacks'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'S001');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'S002', 'Chocolate con leche 30 g', 'Tabla de 30 g', 2.50, 80, 12, id_categoria FROM categoria WHERE descripcion = 'Snacks'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'S002');
INSERT INTO producto (codigo, nombre, descripcion, precio, stock, stock_minimo, id_categoria)
SELECT 'C001', 'Leche evaporada 400 g', 'Lata de 400 g', 4.20, 55, 10, id_categoria FROM categoria WHERE descripcion = 'Lacteos y huevos'
AND NOT EXISTS (SELECT 1 FROM producto WHERE codigo = 'C001');

-- =====================================================================
-- Costo de compra de los productos de ejemplo (80% del precio de venta).
-- Se usa para calcular la UTILIDAD en el reporte ingresos/utilidad/stock.
-- =====================================================================
UPDATE producto SET costo = ROUND(precio * 0.80, 2);

-- =====================================================================
-- USUARIO DE LA APLICACION (util.Conexion usa elsol / elsol123).
-- Se crea aqui para que en una maquina limpia el WAR funcione tal cual.
-- =====================================================================
CREATE USER IF NOT EXISTS 'elsol'@'localhost' IDENTIFIED BY 'elsol123';
GRANT SELECT, INSERT, UPDATE, DELETE ON elsol_db.* TO 'elsol'@'localhost';
FLUSH PRIVILEGES;
