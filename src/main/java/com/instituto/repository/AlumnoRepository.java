package com.instituto.repository;

import com.instituto.model.Alumno;
import com.instituto.model.EstadoAlumno;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    boolean existsByDni(String dni);

    List<Alumno> findByEstado(EstadoAlumno estado);

    // Búsqueda por apellido, nombre o DNI (solo alumnos activos)
    @Query("""
           SELECT a FROM Alumno a
           WHERE a.estado = :estado
             AND (LOWER(a.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.nombre)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR a.dni             LIKE CONCAT('%', :q, '%'))
           """)
    List<Alumno> buscar(@Param("estado") EstadoAlumno estado, @Param("q") String q);
}
