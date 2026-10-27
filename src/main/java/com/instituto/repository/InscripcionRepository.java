package com.instituto.repository;

import com.instituto.model.Inscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    boolean existsByAlumnoIdAndCursoId(Long alumnoId, Long cursoId);

    boolean existsByAlumnoIdAndCursoCicloLectivoId(Long alumnoId, Long cicloLectivoId);

    long countByCursoId(Long cursoId);

    List<Inscripcion> findByCursoId(Long cursoId);
}
