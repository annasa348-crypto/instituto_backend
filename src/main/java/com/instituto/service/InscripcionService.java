package com.instituto.service;

import com.instituto.exception.RecursoNoEncontradoException;
import com.instituto.exception.ReglaNegocioException;
import com.instituto.model.Alumno;
import com.instituto.model.Curso;
import com.instituto.model.EstadoAlumno;
import com.instituto.model.Inscripcion;
import com.instituto.repository.AlumnoRepository;
import com.instituto.repository.CursoRepository;
import com.instituto.repository.InscripcionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepo;
    private final AlumnoRepository alumnoRepo;
    private final CursoRepository cursoRepo;

    public InscripcionService(InscripcionRepository inscripcionRepo,
                              AlumnoRepository alumnoRepo,
                              CursoRepository cursoRepo) {
        this.inscripcionRepo = inscripcionRepo;
        this.alumnoRepo = alumnoRepo;
        this.cursoRepo = cursoRepo;
    }

    /** Proceso de negocio: registrar inscripción aplicando las reglas del instituto. */
    @Transactional
    public Inscripcion inscribir(Long alumnoId, Long cursoId) {
        // Regla: el alumno debe existir y estar activo
        Alumno alumno = alumnoRepo.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el alumno con id " + alumnoId));
        if (alumno.getEstado() != EstadoAlumno.ACTIVO) {
            throw new ReglaNegocioException("El alumno está inactivo y no puede inscribirse");
        }

        // Regla: el curso (y por lo tanto su ciclo lectivo) debe existir
        Curso curso = cursoRepo.findById(cursoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el curso con id " + cursoId));

        // Regla: un alumno no puede tener dos inscripciones en el mismo ciclo lectivo
        if (inscripcionRepo.existsByAlumnoIdAndCursoCicloLectivoId(alumnoId, curso.getCicloLectivo().getId())) {
            throw new ReglaNegocioException("El alumno ya tiene una inscripción en el ciclo lectivo "
                    + curso.getCicloLectivo().getAnio());
        }

        // Regla: un curso no puede superar su capacidad máxima
        if (inscripcionRepo.countByCursoId(cursoId) >= curso.getCapacidadMaxima()) {
            throw new ReglaNegocioException("El curso no tiene cupos disponibles");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        inscripcion.setFecha(LocalDate.now());
        return inscripcionRepo.save(inscripcion);
    }

    public List<Inscripcion> listarPorCurso(Long cursoId) {
        return inscripcionRepo.findByCursoId(cursoId);
    }
}
