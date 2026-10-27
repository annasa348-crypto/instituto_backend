-- Datos de prueba: se pueden ejecutar varias veces sin duplicar
INSERT INTO ciclo_lectivo (anio) VALUES (2026) ON CONFLICT (anio) DO NOTHING;
INSERT INTO ciclo_lectivo (anio) VALUES (2027) ON CONFLICT (anio) DO NOTHING;

INSERT INTO curso (nombre, capacidad_maxima, ciclo_lectivo_id)
SELECT '1° Año A', 30, id FROM ciclo_lectivo WHERE anio = 2026
AND NOT EXISTS (SELECT 1 FROM curso WHERE nombre = '1° Año A');

INSERT INTO curso (nombre, capacidad_maxima, ciclo_lectivo_id)
SELECT '1° Año B', 30, id FROM ciclo_lectivo WHERE anio = 2026
AND NOT EXISTS (SELECT 1 FROM curso WHERE nombre = '1° Año B');
