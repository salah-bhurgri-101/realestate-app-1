-- =====================================================================
-- Flyway Migration: V1__init_property_service_schema.sql
-- Place this file in: src/main/resources/db/migration/
-- Target DB: MySQL 8.x / 9.x
--
-- UUIDs are stored as BINARY(16) because Hibernate 6.2+/7.x maps
-- java.util.UUID to BINARY(16) on MySQL by default (not CHAR(36) like
-- older Hibernate versions). If you'd rather keep CHAR(36) columns,
-- set this in application.properties instead of using this file:
--   hibernate.type.preferred_uuid_jdbc_type=CHAR
-- =====================================================================

-- ---------------------------------------------------------------------
-- Table: properties
-- ---------------------------------------------------------------------
CREATE TABLE properties (
                            id              BINARY(16)      NOT NULL,
                            seller_id       BINARY(16)      NOT NULL,
                            title           VARCHAR(255)    NOT NULL,
                            description     VARCHAR(5000),
                            property_type   VARCHAR(50)     NOT NULL,
                            price           DECIMAL(19,2)   NOT NULL,
                            area            DOUBLE,
                            bedrooms        INT,
                            bathrooms       INT,
                            floors          INT,
                            city            VARCHAR(255)    NOT NULL,
                            area_name       VARCHAR(255),
                            address         VARCHAR(255),
                            latitude        DOUBLE,
                            longitude       DOUBLE,
                            status          VARCHAR(50),
                            featured        BOOLEAN,
                            active          BOOLEAN,
                            total_views     BIGINT,
                            created_at      DATETIME,
                            updated_at      DATETIME,
                            PRIMARY KEY (id),
                            KEY idx_property_city   (city),
                            KEY idx_property_status (status),
                            KEY idx_property_type   (property_type),
                            KEY idx_property_seller (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: property_images
-- ---------------------------------------------------------------------
CREATE TABLE property_images (
                                 id              BINARY(16)      NOT NULL,
                                 image_url       VARCHAR(255)    NOT NULL,
                                 cover_image     BOOLEAN,
                                 file_name       VARCHAR(255),
                                 file_size       BIGINT,
                                 sort_order      INT,
                                 property_id     BINARY(16)      NOT NULL,
                                 PRIMARY KEY (id),
                                 KEY idx_property_images_property_id (property_id),
                                 CONSTRAINT fk_property_images_property
                                     FOREIGN KEY (property_id) REFERENCES properties (id)
                                         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: property_amenities
-- ---------------------------------------------------------------------
CREATE TABLE property_amenities (
                                    id              BINARY(16)      NOT NULL,
                                    amenity         VARCHAR(50)     NOT NULL,
                                    property_id     BINARY(16)      NOT NULL,
                                    PRIMARY KEY (id),
                                    KEY idx_property_amenities_property_id (property_id),
                                    CONSTRAINT fk_property_amenities_property
                                        FOREIGN KEY (property_id) REFERENCES properties (id)
                                            ON DELETE CASCADE,
                                    CONSTRAINT uq_property_amenity UNIQUE (property_id, amenity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: property_reviews
-- Note: propertyId is a plain UUID field in the entity (no @ManyToOne),
-- but an FK is added here for referential integrity at the DB level.
-- Drop it if reviews need to stay decoupled from the properties table.
-- ---------------------------------------------------------------------
CREATE TABLE property_reviews (
                                  id              BINARY(16)      NOT NULL,
                                  buyer_id        BINARY(16)      NOT NULL,
                                  property_id     BINARY(16)      NOT NULL,
                                  comment         VARCHAR(1000),
                                  rating          INT             NOT NULL,
                                  created_at      DATETIME,
                                  PRIMARY KEY (id),
                                  KEY idx_property_reviews_property_id (property_id),
                                  KEY idx_property_reviews_buyer_id    (buyer_id),
                                  CONSTRAINT fk_property_reviews_property
                                      FOREIGN KEY (property_id) REFERENCES properties (id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT chk_property_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: property_views
-- Note: same reasoning as property_reviews regarding the FK.
-- ---------------------------------------------------------------------
CREATE TABLE property_views (
                                id              BINARY(16)      NOT NULL,
                                property_id     BINARY(16)      NOT NULL,
                                user_id         BINARY(16),
                                ip_address      VARCHAR(255),
                                viewed_at       DATETIME,
                                PRIMARY KEY (id),
                                KEY idx_property_views_property_id (property_id),
                                KEY idx_property_views_user_id      (user_id),
                                CONSTRAINT fk_property_views_property
                                    FOREIGN KEY (property_id) REFERENCES properties (id)
                                        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: favorites
-- ---------------------------------------------------------------------
CREATE TABLE favorites (
                           id              BINARY(16)      NOT NULL,
                           buyer_id        BINARY(16)      NOT NULL,
                           property_id     BINARY(16)      NOT NULL,
                           created_at      DATETIME,
                           PRIMARY KEY (id),
                           KEY idx_favorites_buyer_id (buyer_id),
                           CONSTRAINT fk_favorites_property
                               FOREIGN KEY (property_id) REFERENCES properties (id)
                                   ON DELETE CASCADE,
                           CONSTRAINT uq_buyer_property UNIQUE (buyer_id, property_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;