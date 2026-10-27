package com.instituto.service;

import com.instituto.dto.PagoDto;
import com.instituto.dto.PagoRequest;
import com.instituto.exception.ReglaNegocioException;
import com.instituto.model.Cuota;
import com.instituto.model.EstadoCuota;
import com.instituto.model.EstadoMatricula;
import com.instituto.model.Pago;
import com.instituto.repository.CuotaRepository;
import com.instituto.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final CuotaRepository cuotaRepo;
    private final CuotaService cuotaService;

    public PagoService(PagoRepository pagoRepo, CuotaRepository cuotaRepo, CuotaService cuotaService) {
        this.pagoRepo = pagoRepo;
        this.cuotaRepo = cuotaRepo;
        this.cuotaService = cuotaService;
    }

    /** Proceso de negocio: registrar el pago (total o a cuenta) de una cuota. */
    @Transactional
    public PagoDto registrar(PagoRequest req, String usuario) {
        Cuota cuota = cuotaService.obtenerEntidad(req.cuotaId());

        if (cuota.getMatricula().getEstado() == EstadoMatricula.CANCELADA) {
            throw new ReglaNegocioException("La matrícula está cancelada, no se pueden registrar pagos");
        }
        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            throw new ReglaNegocioException("La cuota ya está pagada");
        }
        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new ReglaNegocioException("La cuota está anulada");
        }

        // Regla: no se puede pagar más que el saldo pendiente (se admiten pagos a cuenta)
        BigDecimal saldo = cuota.getImporte().subtract(cuotaService.totalPagado(cuota.getId()));
        if (req.importe().compareTo(saldo) > 0) {
            throw new ReglaNegocioException(
                    "El importe supera el saldo pendiente de la cuota (saldo: " + saldo + ")");
        }

        Pago pago = new Pago();
        pago.setCuota(cuota);
        pago.setFecha(LocalDate.now());
        pago.setImporte(req.importe());
        pago.setMedioPago(req.medioPago());
        pago.setRegistradoPor(usuario);
        pago = pagoRepo.save(pago);

        // Regla: la cuota queda PAGADA cuando se cubre todo su importe
        if (saldo.subtract(req.importe()).signum() == 0) {
            cuota.setEstado(EstadoCuota.PAGADA);
            cuotaRepo.save(cuota);
        }
        return PagoDto.de(pago);
    }

    /** Consulta de pagos por alumno, por cuota y/o por período (todos los filtros son opcionales). */
    @Transactional(readOnly = true)
    public List<PagoDto> listar(Long alumnoId, Long cuotaId, LocalDate desde, LocalDate hasta) {
        List<Pago> base;
        if (cuotaId != null) {
            base = pagoRepo.findByCuotaId(cuotaId);
        } else if (alumnoId != null) {
            base = pagoRepo.findByCuotaMatriculaAlumnoId(alumnoId);
        } else {
            base = pagoRepo.findAll();
        }
        return base.stream()
                .filter(p -> alumnoId == null || p.getCuota().getMatricula().getAlumno().getId().equals(alumnoId))
                .filter(p -> desde == null || !p.getFecha().isBefore(desde))
                .filter(p -> hasta == null || !p.getFecha().isAfter(hasta))
                .sorted(Comparator.comparing(Pago::getFecha).thenComparing(Pago::getId))
                .map(PagoDto::de)
                .toList();
    }
}
