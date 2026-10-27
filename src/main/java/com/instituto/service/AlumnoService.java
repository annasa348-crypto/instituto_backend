package com.instituto.service;

import com.instituto.exception.RecursoNoEncontradoException;
import com.instituto.exception.ReglaNegocioException;
import com.instituto.model.Alumno;
import com.instituto.model.EstadoAlumno;
import com.instituto.repository.AlumnoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlumnoService {

    private final AlumnoRepository repo;

    public AlumnoService(AlumnoRepository repo) {
        this.repo = repo;
    }

    public List<Alumno> listar(String q) {
        if (q == null || q.isBlank()) {
            return repo.findByEstado(EstadoAlumno.ACTIVO);
        }
        return repo.buscar(EstadoAlumno.ACTIVO, q.trim());
    }

    public Alumno obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el alumno con id " + id));
    }

    @Transactional
    public Alumno crear(Alumno alumno) {
        if (repo.existsByDni(alumno.getDni())) {
            throw new ReglaNegocioException("Ya existe un alumno con el DNI " + alumno.getDni());
        }
        alumno.setId(null);
        alumno.setEstado(EstadoAlumno.ACTIVO);
        if (alumno.getFechaIngreso() == null) {
            alumno.setFechaIngreso(LocalDate.now());
        }
        return repo.save(alumno);
    }

    @Transactional
    public Alumno actualizar(Long id, Alumno datos) {
        Alumno existente = obtener(id);
        if (!existente.getDni().equals(datos.getDni()) && repo.existsByDni(datos.getDni())) {
            throw new ReglaNegocioException("Ya existe otro alumno con el DNI " + datos.getDni());
        }
        existente.setDni(datos.getDni());
        existente.setApellido(datos.getApellido());
        existente.setNombre(datos.getNombre());
        existente.setFechaNacimiento(datos.getFechaNacimiento());
        existente.setDomicilio(datos.getDomicilio());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());
        return repo.save(existente);
    }

    // Baja lógica: el alumno puede tener historial académico/administrativo, no se borra.
    @Transactional
    public void darDeBaja(Long id) {
        Alumno alumno = obtener(id);
        alumno.setEstado(EstadoAlumno.INACTIVO);
        repo.save(alumno);
    }
}
