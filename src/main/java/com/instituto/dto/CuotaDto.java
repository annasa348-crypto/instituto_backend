package com.instituto.dto;

import com.instituto.model.Cuota;
import com.instituto.model.EstadoCuota;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaDto(
        Long id,
        Integer numero,
        Long matriculaId,
        Long alumnoId,
        String alumno,
        LocalDate fechaVencimiento,
        BigDecimal importe,
        BigDecimal totalPagado,
        BigDecimal saldo,
        EstadoCuota estado) {

    public static CuotaDto de(Cuota c, BigDecimal totalPagado) {
        BigDecimal saldo = c.getEstado() == EstadoCuota.ANULADA
                ? BigDecimal.ZERO
                : c.getImporte().subtract(totalPagado);
        return new CuotaDto(
                c.getId(),
                c.getNumero(),
                c.getMatricula().getId(),
                c.getMatricula().getAlumno().getId(),
                c.getMatricula().getAlumno().getApellido() + ", " + c.getMatricula().getAlumno().getNombre(),
                c.getFechaVencimiento(),
                c.getImporte(),
                totalPagado,
                saldo,
                c.getEstado());
    }
}
