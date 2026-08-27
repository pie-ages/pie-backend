-- =============================================================
-- V1 — customer, company
-- Depende de: nada (tabelas raiz do domínio)
--
-- Boas práticas:
-- * uuid como PK — sem sequência exposta na URL, mais seguro
-- * timestamptz — armazena fuso horário, correto para apps distribuídos
-- * email UNIQUE em ambas as tabelas — identificador de login
-- * password_hash — nunca texto puro (LGPD + segurança)
-- * company.active boolean — desativa empresa sem deletar dados
-- =============================================================

-- customer — usuário final da plataforma (LGPD: dados pessoais)
CREATE TABLE customer (
    id            uuid         PRIMARY KEY,
    name          varchar,
    email         varchar      UNIQUE,
    password_hash varchar,
    photo_url     varchar,
    created_at    timestamptz,
    updated_at    timestamptz
);

COMMENT ON TABLE  customer               IS 'Usuários finais da plataforma PIE. Dado pessoal — LGPD.';
COMMENT ON COLUMN customer.email         IS 'Dado pessoal (LGPD). Identificador de login único.';
COMMENT ON COLUMN customer.password_hash IS 'Hash BCrypt da senha. NUNCA texto puro.';

-- company — empresa parceira que cadastra produtos
CREATE TABLE company (
    id                 uuid         PRIMARY KEY,
    name               varchar,
    cnpj               varchar      UNIQUE,
    website            varchar,
    social_reason      varchar,
    responsible_person varchar,
    email              varchar      UNIQUE,
    password_hash      varchar,
    active             boolean      DEFAULT true,
    photo_url          varchar,
    created_at         timestamptz,
    updated_at         timestamptz
);

COMMENT ON TABLE  company               IS 'Empresas parceiras da plataforma PIE. Dado sensível — LGPD.';
COMMENT ON COLUMN company.cnpj          IS 'Formato: XX.XXX.XXX/XXXX-XX. Dado sensível (LGPD).';
COMMENT ON COLUMN company.password_hash IS 'Hash BCrypt da senha. NUNCA texto puro.';
