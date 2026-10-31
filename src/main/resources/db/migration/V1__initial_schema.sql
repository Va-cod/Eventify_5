-- Creación de la tabla de Lugares / Recintos (Venues)
CREATE TABLE venues (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(80) NOT NULL,
                        address VARCHAR(80) NOT NULL,
                        capacity INT NOT NULL,
                        city VARCHAR(80) NOT NULL,
                        status BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Creación de la tabla de Eventos (Estructura inicial)
CREATE TABLE events (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(150) NOT NULL,
                        date DATE NOT NULL,
                        description TEXT,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        venue_id BIGINT NOT NULL,
                        CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para optimizar búsquedas y ordenamiento inicial
CREATE INDEX idx_events_date ON events(date);
CREATE INDEX idx_venues_city ON venues(city);