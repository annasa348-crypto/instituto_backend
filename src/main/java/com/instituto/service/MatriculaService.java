package com.instituto.service;

import com.instituto.dto.CuotaDto;
import com.instituto.dto.GenerarCuotasRequest;
import com.instituto.dto.MatriculaRequest;
import com.instituto.exception.RecursoNoEncontradoException;
import com.instituto.exception.ReglaNegocioException;
import com.instituto.model.*;
import com.instituto.repository.CicloLectivoRepository;
import com.instituto.repository.CuotaRepository;
import com.instituto.repository.MatriculaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepo;
    private final CicloLectivoRepository cicloRepo;
    private final CuotaRepository cuotaRepo;
    private final AlumnoService alumnoService;
    private final CuotaService cuotaService;

    public MatriculaService(MatriculaRepository matriculaRepo, CicloLectivoRepository cicloRepo,
                            CuotaRepository cuotaRepo, AlumnoService alumnoService,
                            CuotaService cuotaService) {
        this.matriculaRepo = matriculaRepo;
        this.cicloRepo = cicloRepo;
        this.cuotaRepo = cuotaRepo;
        this.alumnoService = alumnoService;
        this.cuotaService = cuotaService;
    }

    @Transactional
    public Matricula crear(MatriculaRequest req) {
        Alumno alumno = alumnoService.obtener(req.alumnoId());
        if (alumno.getEstado() != EstadoAlumno.ACTIVO) {
            throw new ReglaNegocioException("El alumno está inactivo y no puede matricularse");
        }
        CicloLectivo ciclo = cicloRepo.findById(req.cicloLectivoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el ciclo lectivo con id " + req.cicloLectivoId()));

        // Regla: una matrícula pertenece a un único alumno y ciclo lectivo
        if (matriculaRepo.existsByAlumnoIdAndCicloLectivoId(alumno.getId(), ciclo.getId())) {
            throw new ReglaNegocioException(
                    "El alumno ya tiene una matrícula en el ciclo lectivo " + ciclo.getAnio());
        }

        Matricula m = new Matricula();
        m.setAlumno(alumno);
        m.setCicloLectivo(ciclo);
        m.setFecha(LocalDate.now());
        m.setImporte(req.importe() == null ? BigDecimal.ZERO : req.importe());
        m.setEstado(EstadoMatricula.PENDIENTE);
        return matriculaRepo.save(m);
    }

    public Matricula obtener(Long id) {
        return matriculaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la matrícula con id " + id));
    }

    public List<Matricula> listarPorCiclo(Long cicloId) {
        if (!cicloRepo.existsById(cicloId)) {
            throw new RecursoNoEncontradoException("No existe el ciclo lectivo con id " + cicloId);
        }
        return matriculaRepo.findByCicloLectivoId(cicloId);
    }

    @Transactional
    public Matricula cambiarEstado(Long id, EstadoMatricula nuevo) {
        Matricula m = obtener(id);
        if (m.getEstado() == EstadoMatricula.CANCELADA && nuevo != EstadoMatricula.CANCELADA) {
            throw new ReglaNegocioException("Una matrícula cancelada no puede reactivarse");
        }
        if (nuevo == EstadoMatricula.CANCELADA && m.getEstado() != EstadoMatricula.CANCELADA) {
            // Al cancelar, se anulan las cuotas sin ningún pago registrado
            for (Cuota c : cuotaRepo.findByMatriculaIdOrderByNumero(id)) {
                boolean abierta = c.getEstado() == EstadoCuota.PENDIENTE || c.getEstado() == EstadoCuota.VENCIDA;
                if (abierta && cuotaService.totalPagado(c.getId()).signum() == 0) {
                    c.setEstado(EstadoCuota.ANULADA);
                    cuotaRepo.save(c);
                }
            }
        }
        m.setEstado(nuevo);
        return matriculaRepo.save(m);
    }

    /** Proceso de negocio: genera N cuotas mensuales a partir del primer vencimiento. */
    @Transactional
    public List<CuotaDto> generarCuotas(Long matriculaId, GenerarCuotasRequest req) {
        Matricula m = obtener(matriculaId);
        if (m.getEstado() == EstadoMatricula.CANCELADA) {
            throw new ReglaNegocioException("No se pueden generar cuotas para una matrícula cancelada");
        }
        if (cuotaRepo.existsByMatriculaId(matriculaId)) {
            throw new ReglaNegocioException("La matrícula ya tiene cuotas generadas");
        }

        List<CuotaDto> generadas = new ArrayList<>();
        for (int i = 1; i <= req.cantidad(); i++) {
            Cuota c = new Cuota();
            c.setMatricula(m);
            c.setNumero(i);
            c.setImporte(req.importeCuota());
            c.setFechaVencimiento(req.primerVencimiento().plusMonths(i - 1L));
            c.setEstado(EstadoCuota.PENDIENTE);
            generadas.add(CuotaDto.de(cuotaRepo.save(c), BigDecimal.ZERO));
        }
        // Si alguna cuota nace con vencimiento ya pasado, se marca como vencida
        cuotaService.actualizarVencidas();
        return cuotaService.listarPorMatricula(matriculaId);
    }
}
