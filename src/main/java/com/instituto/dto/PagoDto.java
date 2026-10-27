package com.instituto.dto;

import com.instituto.model.MedioPago;
import com.instituto.model.Pago;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoDto(
        Long id,
        Long cuotaId,
        Integer numeroCuota,
        Long alumnoId,
        String alumno,
        LocalDate fecha,
        BigDecimal importe,
        MedioPago medioPago,
        String registradoPor) {

    public static PagoDto de(Pago p) {
        var alumno = p.getCuota().getMatricula().getAlumno();
        return new PagoDto(
                p.getId(),
                p.getCuota().getId(),
                p.getCuota().getNumero(),
                alumno.getId(),
                alumno.getApellido() + ", " + alumno.getNombre(),
                p.getFecha(),
                p.getImporte(),
                p.getMedioPago(),
                p.getRegistradoPor());
    }
}
