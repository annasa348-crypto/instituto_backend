package com.instituto.repository;

import com.instituto.model.Matricula;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAlumnoIdAndCicloLectivoId(Long alumnoId, Long cicloLectivoId);

    List<Matricula> findByCicloLectivoId(Long cicloLectivoId);

    List<Matricula> findByAlumnoIdOrderByCicloLectivoAnio(Long alumnoId);
}
