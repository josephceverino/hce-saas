-- Script de creación de la base de datos (SQL Server)
-- Generado a partir del modelo lógico 3.4.2 (3FN). Revisar antes de ejecutar.

CREATE DATABASE hce_saas;
GO
USE hce_saas;
GO

CREATE TABLE establecimiento (
    id_establecimiento INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    codigo_ipress VARCHAR(20) NOT NULL,
    region VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_establecimiento PRIMARY KEY (id_establecimiento)
);
GO

CREATE TABLE suscripcion (
    id_suscripcion INT IDENTITY(1,1) NOT NULL,
    id_establecimiento INT NOT NULL,
    [plan] VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_suscripcion PRIMARY KEY (id_suscripcion)
);
GO

CREATE TABLE rol (
    id_rol INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol)
);
GO

CREATE TABLE permiso (
    id_permiso INT IDENTITY(1,1) NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT pk_permiso PRIMARY KEY (id_permiso)
);
GO

CREATE TABLE rol_permiso (
    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,
    CONSTRAINT pk_rol_permiso PRIMARY KEY (id_rol, id_permiso)
);
GO

CREATE TABLE usuario (
    id_usuario INT IDENTITY(1,1) NOT NULL,
    id_establecimiento INT NULL,
    id_rol INT NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    ultimo_acceso DATETIME NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario)
);
GO

CREATE TABLE profesional_salud (
    id_profesional_salud INT IDENTITY(1,1) NOT NULL,
    id_usuario INT NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    colegiatura VARCHAR(20) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    CONSTRAINT pk_profesional_salud PRIMARY KEY (id_profesional_salud)
);
GO

CREATE TABLE paciente (
    id_paciente INT IDENTITY(1,1) NOT NULL,
    id_usuario INT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    nro_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    sexo VARCHAR(10) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    CONSTRAINT pk_paciente PRIMARY KEY (id_paciente)
);
GO

CREATE TABLE representante_legal (
    id_representante_legal INT IDENTITY(1,1) NOT NULL,
    id_paciente INT NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    vigencia DATE NOT NULL,
    CONSTRAINT pk_representante_legal PRIMARY KEY (id_representante_legal)
);
GO

CREATE TABLE historia_clinica (
    id_historia_clinica INT IDENTITY(1,1) NOT NULL,
    id_paciente INT NOT NULL,
    id_establecimiento INT NOT NULL,
    nro_hc VARCHAR(20) NOT NULL,
    fecha_apertura DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_historia_clinica PRIMARY KEY (id_historia_clinica)
);
GO

CREATE TABLE atencion (
    id_atencion INT IDENTITY(1,1) NOT NULL,
    id_historia_clinica INT NOT NULL,
    id_profesional_salud INT NOT NULL,
    id_establecimiento INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo TEXT NOT NULL,
    anamnesis TEXT NOT NULL,
    examen_fisico TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_atencion PRIMARY KEY (id_atencion)
);
GO

CREATE TABLE antecedente (
    id_antecedente INT IDENTITY(1,1) NOT NULL,
    id_historia_clinica INT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro DATE NOT NULL,
    CONSTRAINT pk_antecedente PRIMARY KEY (id_antecedente)
);
GO

CREATE TABLE alergia (
    id_alergia INT IDENTITY(1,1) NOT NULL,
    id_historia_clinica INT NOT NULL,
    agente VARCHAR(100) NOT NULL,
    reaccion VARCHAR(200) NOT NULL,
    gravedad VARCHAR(20) NOT NULL,
    CONSTRAINT pk_alergia PRIMARY KEY (id_alergia)
);
GO

CREATE TABLE diagnostico (
    id_diagnostico INT IDENTITY(1,1) NOT NULL,
    id_atencion INT NOT NULL,
    codigo_cie10 VARCHAR(10) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    CONSTRAINT pk_diagnostico PRIMARY KEY (id_diagnostico)
);
GO

CREATE TABLE tratamiento (
    id_tratamiento INT IDENTITY(1,1) NOT NULL,
    id_atencion INT NOT NULL,
    indicacion TEXT NOT NULL,
    medicamento VARCHAR(150) NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    duracion VARCHAR(100) NOT NULL,
    CONSTRAINT pk_tratamiento PRIMARY KEY (id_tratamiento)
);
GO

CREATE TABLE resultado_examen (
    id_resultado_examen INT IDENTITY(1,1) NOT NULL,
    id_atencion INT NOT NULL,
    id_profesional_salud INT NOT NULL,
    tipo_examen VARCHAR(100) NOT NULL,
    fecha DATE NOT NULL,
    resultado TEXT NOT NULL,
    archivo_adjunto VARCHAR(255) NOT NULL,
    CONSTRAINT pk_resultado_examen PRIMARY KEY (id_resultado_examen)
);
GO

CREATE TABLE enmienda (
    id_enmienda INT IDENTITY(1,1) NOT NULL,
    id_atencion INT NOT NULL,
    id_profesional_salud INT NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    contenido_corregido TEXT NOT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT pk_enmienda PRIMARY KEY (id_enmienda)
);
GO

CREATE TABLE consentimiento_acceso (
    id_consentimiento_acceso INT IDENTITY(1,1) NOT NULL,
    id_paciente INT NOT NULL,
    otorgado_a VARCHAR(150) NOT NULL,
    alcance VARCHAR(200) NOT NULL,
    fecha_otorgado DATETIME NOT NULL,
    fecha_revocado DATETIME NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_consentimiento_acceso PRIMARY KEY (id_consentimiento_acceso)
);
GO

CREATE TABLE registro_auditoria (
    id_registro_auditoria INT IDENTITY(1,1) NOT NULL,
    id_usuario INT NOT NULL,
    id_historia_clinica INT NULL,
    fecha_hora DATETIME NOT NULL,
    accion VARCHAR(100) NOT NULL,
    entidad_afectada VARCHAR(50) NOT NULL,
    id_entidad_afectada INT NOT NULL,
    ip VARCHAR(45) NOT NULL,
    CONSTRAINT pk_registro_auditoria PRIMARY KEY (id_registro_auditoria)
);
GO

CREATE TABLE solicitud_intercambio (
    id_solicitud_intercambio INT IDENTITY(1,1) NOT NULL,
    id_historia_clinica INT NOT NULL,
    id_establecimiento_origen INT NOT NULL,
    id_establecimiento_destino INT NOT NULL,
    fecha DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    justificacion TEXT NOT NULL,
    CONSTRAINT pk_solicitud_intercambio PRIMARY KEY (id_solicitud_intercambio)
);
GO

ALTER TABLE suscripcion ADD CONSTRAINT fk_suscripcion_id_establecimiento FOREIGN KEY (id_establecimiento) REFERENCES establecimiento(id_establecimiento);
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_id_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol);
ALTER TABLE rol_permiso ADD CONSTRAINT fk_rol_permiso_id_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso);
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_id_establecimiento FOREIGN KEY (id_establecimiento) REFERENCES establecimiento(id_establecimiento);
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_id_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol);
ALTER TABLE profesional_salud ADD CONSTRAINT fk_profesional_salud_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
ALTER TABLE paciente ADD CONSTRAINT fk_paciente_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
ALTER TABLE representante_legal ADD CONSTRAINT fk_representante_legal_id_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente);
ALTER TABLE historia_clinica ADD CONSTRAINT fk_historia_clinica_id_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente);
ALTER TABLE historia_clinica ADD CONSTRAINT fk_historia_clinica_id_establecimiento FOREIGN KEY (id_establecimiento) REFERENCES establecimiento(id_establecimiento);
ALTER TABLE atencion ADD CONSTRAINT fk_atencion_id_historia_clinica FOREIGN KEY (id_historia_clinica) REFERENCES historia_clinica(id_historia_clinica);
ALTER TABLE atencion ADD CONSTRAINT fk_atencion_id_profesional_salud FOREIGN KEY (id_profesional_salud) REFERENCES profesional_salud(id_profesional_salud);
ALTER TABLE atencion ADD CONSTRAINT fk_atencion_id_establecimiento FOREIGN KEY (id_establecimiento) REFERENCES establecimiento(id_establecimiento);
ALTER TABLE antecedente ADD CONSTRAINT fk_antecedente_id_historia_clinica FOREIGN KEY (id_historia_clinica) REFERENCES historia_clinica(id_historia_clinica);
ALTER TABLE alergia ADD CONSTRAINT fk_alergia_id_historia_clinica FOREIGN KEY (id_historia_clinica) REFERENCES historia_clinica(id_historia_clinica);
ALTER TABLE diagnostico ADD CONSTRAINT fk_diagnostico_id_atencion FOREIGN KEY (id_atencion) REFERENCES atencion(id_atencion);
ALTER TABLE tratamiento ADD CONSTRAINT fk_tratamiento_id_atencion FOREIGN KEY (id_atencion) REFERENCES atencion(id_atencion);
ALTER TABLE resultado_examen ADD CONSTRAINT fk_resultado_examen_id_atencion FOREIGN KEY (id_atencion) REFERENCES atencion(id_atencion);
ALTER TABLE resultado_examen ADD CONSTRAINT fk_resultado_examen_id_profesional_salud FOREIGN KEY (id_profesional_salud) REFERENCES profesional_salud(id_profesional_salud);
ALTER TABLE enmienda ADD CONSTRAINT fk_enmienda_id_atencion FOREIGN KEY (id_atencion) REFERENCES atencion(id_atencion);
ALTER TABLE enmienda ADD CONSTRAINT fk_enmienda_id_profesional_salud FOREIGN KEY (id_profesional_salud) REFERENCES profesional_salud(id_profesional_salud);
ALTER TABLE consentimiento_acceso ADD CONSTRAINT fk_consentimiento_acceso_id_paciente FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente);
ALTER TABLE registro_auditoria ADD CONSTRAINT fk_registro_auditoria_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
ALTER TABLE registro_auditoria ADD CONSTRAINT fk_registro_auditoria_id_historia_clinica FOREIGN KEY (id_historia_clinica) REFERENCES historia_clinica(id_historia_clinica);
ALTER TABLE solicitud_intercambio ADD CONSTRAINT fk_solicitud_intercambio_id_historia_clinica FOREIGN KEY (id_historia_clinica) REFERENCES historia_clinica(id_historia_clinica);
ALTER TABLE solicitud_intercambio ADD CONSTRAINT fk_solicitud_intercambio_id_establecimiento_origen FOREIGN KEY (id_establecimiento_origen) REFERENCES establecimiento(id_establecimiento);
ALTER TABLE solicitud_intercambio ADD CONSTRAINT fk_solicitud_intercambio_id_establecimiento_destino FOREIGN KEY (id_establecimiento_destino) REFERENCES establecimiento(id_establecimiento);
GO

-- Reglas de unicidad que reflejan las reglas de negocio
ALTER TABLE paciente ADD CONSTRAINT uq_paciente_documento UNIQUE (tipo_documento, nro_documento); -- RN-01
ALTER TABLE historia_clinica ADD CONSTRAINT uq_historia_paciente UNIQUE (id_paciente); -- RN-01
ALTER TABLE historia_clinica ADD CONSTRAINT uq_historia_nro UNIQUE (nro_hc);
ALTER TABLE profesional_salud ADD CONSTRAINT uq_profesional_usuario UNIQUE (id_usuario);
ALTER TABLE usuario ADD CONSTRAINT uq_usuario_nombre UNIQUE (usuario);
GO
