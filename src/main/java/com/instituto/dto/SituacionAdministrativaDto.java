package com.instituto.dto;

import java.math.BigDecimal;
import java.util.List;

public record SituacionAdministrativaDto(
        Long alumnoId,
        String alumno,
        List<MatriculaSituacionDto> matriculas,
        long cuotasPendientes,
        long cuotasVencidas,
        BigDecimal importeAdeudado,
        BigDecimal totalAbonado) {
}
