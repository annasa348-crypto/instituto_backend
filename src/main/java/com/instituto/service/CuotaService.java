package com.instituto.service;

import com.instituto.dto.CuotaDto;
import com.instituto.dto.MatriculaSituacionDto;
import com.instituto.dto.SituacionAdministrativaDto;
import com.instituto.exception.RecursoNoEncontradoException;
import com.instituto.model.Alumno;
import com.instituto.model.Cuota;
import com.instituto.model.EstadoCuota;
import com.instituto.model.Matricula;
import com.instituto.repository.CuotaRepository;
import com.instituto.repository.MatriculaRepository;
import com.instituto.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuotaService {

    private final CuotaRepository cuotaRepo;
    private final PagoRepository pagoRepo;
    private final MatriculaRepository matriculaRepo;
    private final AlumnoService alumnoService;

    public CuotaService(CuotaRepository cuotaRepo, PagoRepository pagoRepo,
                        MatriculaRepository matriculaRepo, AlumnoService alumnoService) {
        this.cuotaRepo = cuotaRepo;
        this.pagoRepo = pagoRepo;
        this.matriculaRepo = matriculaRepo;
        this.alumnoService = alumnoService;
    }

    /** Regla: una cuota PENDIENTE cuyo vencimiento ya pasó se considera VENCIDA. */
    @Transactional
    public void actualizarVencidas() {
        cuotaRepo.marcarVencidas(LocalDate.now(), EstadoCuota.PENDIENTE, EstadoCuota.VENCIDA);
    }

    public BigDecimal totalPagado(Long cuotaId) {
        BigDecimal total = pagoRepo.sumaPorCuota(cuotaId);
        return total == null ? BigDecimal.ZERO : total;
    }

    public CuotaDto aDto(Cuota cuota) {
        return CuotaDto.de(cuota, totalPagado(cuota.getId()));
    }

    @Transactional
    public Cuota obtenerEntidad(Long id) {
        actualizarVencidas();
        return cuotaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuota con id " + id));
    }

    @Transactional
    public CuotaDto obtener(Long id) {
        return aDto(obtenerEntidad(id));
    }

    @Transactional
    public List<CuotaDto> listarPorMatricula(Long matriculaId) {
        actualizarVencidas();
        return cuotaRepo.findByMatriculaIdOrderByNumero(matriculaId).stream().map(this::aDto).toList();
    }

    @Transactional
    public List<CuotaDto> listarPorAlumno(Long alumnoId) {
        alumnoService.obtener(alumnoId);
        actualizarVencidas();
        return cuotaRepo.porAlumno(alumnoId).stream().map(this::aDto).toList();
    }

    @Transactional
    public List<CuotaDto> listarVencidas() {
        actualizarVencidas();
        return cuotaRepo.findByEstadoOrderByFechaVencimiento(EstadoCuota.VENCIDA)
                .stream().map(this::aDto).toList();
    }

    /** Situación administrativa: matrículas, cuotas y totales de deuda/pagos del alumno. */
    @Transactional
    public SituacionAdministrativaDto situacion(Long alumnoId) {
        Alumno alumno = alumnoService.obtener(alumnoId);
        actualizarVencidas();

        List<MatriculaSituacionDto> matriculas = new ArrayList<>();
        long pendientes = 0;
        long vencidas = 0;
        BigDecimal adeudado = BigDecimal.ZERO;
        BigDecimal abonado = BigDecimal.ZERO;

        for (Matricula m : matriculaRepo.findByAlumnoIdOrderByCicloLectivoAnio(alumnoId)) {
            List<CuotaDto> cuotas = cuotaRepo.findByMatriculaIdOrderByNumero(m.getId())
                    .stream().map(this::aDto).toList();
            for (CuotaDto c : cuotas) {
                if (c.estado() == EstadoCuota.PENDIENTE) pendientes++;
                if (c.estado() == EstadoCuota.VENCIDA) vencidas++;
                if (c.estado() == EstadoCuota.PENDIENTE || c.estado() == EstadoCuota.VENCIDA) {
                    adeudado = adeudado.add(c.saldo());
                }
                abonado = abonado.add(c.totalPagado());
            }
            matriculas.add(new MatriculaSituacionDto(
                    m.getId(), m.getCicloLectivo().getAnio(), m.getEstado(), cuotas));
        }

        return new SituacionAdministrativaDto(
                alumno.getId(),
                alumno.getApellido() + ", " + alumno.getNombre(),
                matriculas, pendientes, vencidas, adeudado, abonado);
    }
}
