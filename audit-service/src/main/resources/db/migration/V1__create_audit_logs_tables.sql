CREATE TABLE audit_logs (
                            id             VARCHAR(36) NOT NULL,
                            service_name   VARCHAR(100) NOT NULL,
                            action         VARCHAR(30) NOT NULL,
                            entity_type    VARCHAR(100),
                            entity_id      VARCHAR(100),
                            username       VARCHAR(150),
                            user_id        VARCHAR(100),
                            ip_address     VARCHAR(45),
                            description    VARCHAR(1000),
                            metadata       TEXT,
                            status         VARCHAR(20) DEFAULT 'SUCCESS',
                            trace_id       VARCHAR(100),
                            created_at     TIMESTAMP NOT NULL,

                            CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_audit_service_name ON audit_logs (service_name);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs (username);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);
-- V2__add_composite_index_service_created.sql
CREATE INDEX idx_audit_service_created ON audit_logs (service_name, created_at DESC);