-- Creación de la tabla de Categorías
CREATE TABLE event_categories (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  name VARCHAR(50) NOT NULL UNIQUE,
                                  description VARCHAR(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Evolución de la tabla 'events': Agregar la relación con Categoría
ALTER TABLE events
    ADD COLUMN category_id BIGINT NOT NULL AFTER venue_id;

-- Clave foránea e índice para optimizar consultas relacionales
ALTER TABLE events
    ADD CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES event_categories(id);

CREATE INDEX idx_events_category ON events(category_id);