-- Script SQL para proyecto PETCARE
DROP DATABASE IF EXISTS petcare;
CREATE DATABASE petcare CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE petcare;

-- Tabla users (similar a la del profesor)
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6) DEFAULT NULL,
  email VARCHAR(255) NOT NULL,
  google_id VARCHAR(255) DEFAULT NULL,
  name VARCHAR(255) NOT NULL,
  picture VARCHAR(255) DEFAULT NULL,
  provider ENUM('FACEBOOK','GITHUB','GOOGLE','LOCAL') DEFAULT 'GOOGLE',
  updated_at DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_users_email (email),
  UNIQUE KEY UK_users_google (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO users (email, google_id, name, picture, provider, created_at, updated_at) VALUES
('admin@petcare.com', 'google-admin-001', 'Admin Petcare', NULL, 'GOOGLE', NOW(), NOW()),
('user1@petcare.com', 'google-user-001', 'Usuario Uno', NULL, 'GOOGLE', NOW(), NOW()),
('user2@petcare.com', 'google-user-002', 'Usuario Dos', NULL, 'GOOGLE', NOW(), NOW());

-- Tablas de dominio: dueno, veterinario, mascota, cita
DROP TABLE IF EXISTS cita;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS veterinario;
DROP TABLE IF EXISTS dueno;

CREATE TABLE dueno (
  id BIGINT NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  telefono VARCHAR(30),
  direccion VARCHAR(255),
  ciudad VARCHAR(100),
  PRIMARY KEY (id),
  UNIQUE KEY UK_dueno_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE veterinario (
  id BIGINT NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  especialidad VARCHAR(100),
  telefono VARCHAR(30),
  email VARCHAR(255),
  direccion VARCHAR(255),
  ciudad VARCHAR(100),
  latitud DOUBLE,
  longitud DOUBLE,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE mascota (
  id BIGINT NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  especie VARCHAR(30) NOT NULL,
  raza VARCHAR(100),
  fecha_nacimiento DATE,
  peso DOUBLE,
  foto_url VARCHAR(255),
  dueno_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_mascota_dueno FOREIGN KEY (dueno_id) REFERENCES dueno(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cita (
  id BIGINT NOT NULL,
  fecha_hora DATETIME NOT NULL,
  motivo VARCHAR(255),
  estado ENUM('PENDIENTE','CONFIRMADA','CANCELADA','COMPLETADA') DEFAULT 'PENDIENTE',
  notas TEXT,
  mascota_id BIGINT NOT NULL,
  veterinario_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT NOW(),
  PRIMARY KEY (id),
  CONSTRAINT FK_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(id),
  CONSTRAINT FK_cita_vet FOREIGN KEY (veterinario_id) REFERENCES veterinario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Datos de prueba (20 dueños)
INSERT INTO dueno (id, nombre, email, telefono, direccion, ciudad) VALUES
(1,'Ana López','ana.lopez@example.com','600111001','Calle Sol 1','Sevilla'),
(2,'Carlos Pérez','carlos.perez@example.com','600111002','Av. Libertad 23','Sevilla'),
(3,'Lucía Gómez','lucia.gomez@example.com','600111003','Calle Luna 5','Dos Hermanas'),
(4,'Miguel Torres','miguel.torres@example.com','600111004','Calle Mar 7','Alcalá'),
(5,'Sara Ruiz','sara.ruiz@example.com','600111005','Plaza Centro 9','Sevilla'),
(6,'Jorge Martín','jorge.martin@example.com','600111006','Calle Nieve 3','Sevilla'),
(7,'Paula Díaz','paula.diaz@example.com','600111007','Av. Sol 11','Dos Hermanas'),
(8,'David Romero','david.romero@example.com','600111008','Calle Aire 4','Sevilla'),
(9,'Elena Navarro','elena.navarro@example.com','600111009','Calle Flores 13','Sevilla'),
(10,'Luis Herrera','luis.herrera@example.com','600111010','Calle Río 6','Coria'),
(11,'Clara Vázquez','clara.vazquez@example.com','600111011','Av. Parque 21','Sevilla'),
(12,'Alberto León','alberto.leon@example.com','600111012','Calle Campo 8','Sevilla'),
(13,'Marta Iglesias','marta.iglesias@example.com','600111013','Plaza Norte 2','Sevilla'),
(14,'Óscar Rubio','oscar.rubio@example.com','600111014','Calle Este 16','Sevilla'),
(15,'Nuria Santos','nuria.santos@example.com','600111015','Calle Oeste 18','Dos Hermanas'),
(16,'Raúl Flores','raul.flores@example.com','600111016','Calle Sur 20','Sevilla'),
(17,'Patricia Gil','patricia.gil@example.com','600111017','Calle Norte 22','Alcalá'),
(18,'Iván Medina','ivan.medina@example.com','600111018','Calle Centro 24','Sevilla'),
(19,'Carmen Roldán','carmen.roldan@example.com','600111019','Calle Jardín 26','Sevilla'),
(20,'Diego Campos','diego.campos@example.com','600111020','Calle Lago 28','Sevilla');

-- 20 veterinarios
INSERT INTO veterinario (id, nombre, especialidad, telefono, email, direccion, ciudad, latitud, longitud) VALUES
(1,'VetSur Centro','General','955111001','info@vetsur1.com','Calle Salud 1','Sevilla',37.3891,-5.9845),
(2,'Clínica Animalia','Perros','955111002','info@animalia.com','Av. Mascotas 3','Sevilla',37.3905,-5.9800),
(3,'Vet Felinos','Gatos','955111003','info@vetfelinos.com','Calle Gato 2','Sevilla',37.3910,-5.9820),
(4,'Vet Caballos','Caballos','955111004','info@vetcaballos.com','Ctra. Campo 7','Dos Hermanas',37.2860,-5.9200),
(5,'Centro Exóticos','Exóticos','955111005','info@exoticos.com','Calle Loro 5','Sevilla',37.3920,-5.9790),
(6,'Vet Norte','General','955111006','info@vetnorte.com','Av. Norte 10','Sevilla',37.4000,-5.9700),
(7,'Vet Sur','Urgencias','955111007','info@vetsur.com','Av. Sur 15','Sevilla',37.3800,-5.9900),
(8,'Vet Dos Hermanas','General','955111008','info@vetdh.com','Calle Centro 8','Dos Hermanas',37.2830,-5.9200),
(9,'Vet Alcalá','General','955111009','info@vetalcala.com','Calle Campo 12','Alcalá',37.3380,-5.8450),
(10,'Vet Coria','General','955111010','info@vetcoria.com','Calle Río 4','Coria',37.2850,-6.0500),
(11,'Vet Felicidad','General','955111011','info@vetfelicidad.com','Calle Paz 9','Sevilla',37.3940,-5.9750),
(12,'Vet Centro','General','955111012','info@vetcentro.com','Plaza Centro 1','Sevilla',37.3899,-5.9850),
(13,'Vet Animales Pequeños','Roedores','955111013','info@vetpeques.com','Calle Ratón 6','Sevilla',37.3895,-5.9810),
(14,'Urgencias 24h','Urgencias','955111014','info@vet24h.com','Av. Urgencias 12','Sevilla',37.3880,-5.9870),
(15,'Vet Mascotas','General','955111015','info@vetmascotas.com','Calle Amigo 7','Sevilla',37.3875,-5.9830),
(16,'Vet Especialistas','Traumatología','955111016','info@vettrauma.com','Calle Hueso 11','Sevilla',37.3930,-5.9780),
(17,'Vet Dental','Odontología','955111017','info@vetdental.com','Calle Diente 4','Sevilla',37.3950,-5.9760),
(18,'Vet Ojos','Oftalmología','955111018','info@vetojos.com','Calle Vista 2','Sevilla',37.3960,-5.9740),
(19,'Vet Piel','Dermatología','955111019','info@vetpiel.com','Calle Piel 8','Sevilla',37.3970,-5.9720),
(20,'Vet Rehabilitación','Rehabilitación','955111020','info@vetrehab.com','Calle Reha 10','Sevilla',37.3980,-5.9710);

-- 20 mascotas
INSERT INTO mascota (id, nombre, especie, raza, fecha_nacimiento, peso, foto_url, dueno_id) VALUES
(1,'Luna','PERRO','Labrador','2020-03-10',25.5,NULL,1),
(2,'Misu','GATO','Europeo','2021-06-01',4.2,NULL,1),
(3,'Rocky','PERRO','Bulldog','2019-09-15',19.0,NULL,2),
(4,'Nala','GATO','Siames','2022-01-20',3.8,NULL,3),
(5,'Toby','PERRO','Golden Retriever','2018-11-05',30.0,NULL,4),
(6,'Kira','PERRO','Border Collie','2020-02-14',18.0,NULL,5),
(7,'Simba','GATO','Persa','2019-07-07',4.5,NULL,6),
(8,'Max','PERRO','Mestizo','2021-04-30',12.3,NULL,7),
(9,'Lola','GATO','Europeo','2020-08-19',3.9,NULL,8),
(10,'Zeus','PERRO','Pastor Alemán','2017-12-25',32.0,NULL,9),
(11,'Coco','PERRO','Chihuahua','2021-09-09',2.1,NULL,10),
(12,'Maya','GATO','Bengalí','2022-03-12',3.7,NULL,11),
(13,'Thor','PERRO','Boxer','2019-10-10',28.0,NULL,12),
(14,'Niebla','PERRO','Husky','2018-05-05',27.5,NULL,13),
(15,'Pecas','GATO','Europeo','2020-11-11',4.1,NULL,14),
(16,'Bruno','PERRO','Mestizo','2021-01-01',15.0,NULL,15),
(17,'Lluvia','GATO','Siames','2019-04-04',3.9,NULL,16),
(18,'Rayo','PERRO','Galgo','2018-09-30',24.0,NULL,17),
(19,'Pelusa','GATO','Persa','2017-07-17',4.8,NULL,18),
(20,'Chispa','PERRO','Mestizo','2022-05-20',8.5,NULL,19);

-- 20 citas
INSERT INTO cita (id, fecha_hora, motivo, estado, notas, mascota_id, veterinario_id, created_at) VALUES
(1,'2025-01-10 10:00:00','Vacuna anual','CONFIRMADA','Traer cartilla',1,1,NOW()),
(2,'2025-01-11 11:30:00','Revisión general','PENDIENTE','',2,2,NOW()),
(3,'2025-01-12 09:15:00','Consulta cojera','PENDIENTE','',3,3,NOW()),
(4,'2025-01-13 17:00:00','Desparasitación','COMPLETADA','Correcto',4,4,NOW()),
(5,'2025-01-14 18:30:00','Vacuna rabia','CONFIRMADA','',5,5,NOW()),
(6,'2025-01-15 16:00:00','Control peso','PENDIENTE','',6,6,NOW()),
(7,'2025-01-16 12:00:00','Revisión ojos','PENDIENTE','',7,18,NOW()),
(8,'2025-01-17 10:45:00','Limpieza dental','PENDIENTE','',8,17,NOW()),
(9,'2025-01-18 19:00:00','Urgencia vómitos','COMPLETADA','Tratado con medicación',9,7,NOW()),
(10,'2025-01-19 09:30:00','Vacuna cachorro','CONFIRMADA','',10,1,NOW()),
(11,'2025-01-20 11:00:00','Picores piel','PENDIENTE','',11,19,NOW()),
(12,'2025-01-21 12:30:00','Revisión post-quirúrgica','PENDIENTE','',12,16,NOW()),
(13,'2025-01-22 17:15:00','Dolor pata trasera','PENDIENTE','',13,16,NOW()),
(14,'2025-01-23 18:00:00','Vacuna leucemia felina','PENDIENTE','',14,3,NOW()),
(15,'2025-01-24 10:15:00','Chequeo senior','PENDIENTE','',15,1,NOW()),
(16,'2025-01-25 09:00:00','Consulta diarrea','PENDIENTE','',16,7,NOW()),
(17,'2025-01-26 13:00:00','Radiografía','PENDIENTE','',17,16,NOW()),
(18,'2025-01-27 15:45:00','Vacuna anual','CONFIRMADA','',18,1,NOW()),
(19,'2025-01-28 16:30:00','Consulta comportamiento','PENDIENTE','',19,5,NOW()),
(20,'2025-01-29 19:30:00','Revisión general','PENDIENTE','',20,2,NOW());

-- Tablas de secuencia que genera Hibernate normalmente (para AUTO)
DROP TABLE IF EXISTS dueno_seq;
CREATE TABLE dueno_seq (next_val BIGINT);
INSERT INTO dueno_seq VALUES (21);

DROP TABLE IF EXISTS veterinario_seq;
CREATE TABLE veterinario_seq (next_val BIGINT);
INSERT INTO veterinario_seq VALUES (21);

DROP TABLE IF EXISTS mascota_seq;
CREATE TABLE mascota_seq (next_val BIGINT);
INSERT INTO mascota_seq VALUES (21);

DROP TABLE IF EXISTS cita_seq;
CREATE TABLE cita_seq (next_val BIGINT);
INSERT INTO cita_seq VALUES (21);
