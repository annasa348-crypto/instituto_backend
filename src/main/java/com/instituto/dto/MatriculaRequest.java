package com.instituto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MatriculaRequest(
        @NotNull(message = "alumnoId es obligatorio") Long alumnoId,
        @NotNull(message = "cicloLectivoId es obligatorio") Long cicloLectivoId,
        @PositiveOrZero(message = "El importe no puede ser negativo") BigDecimal importe) {
}
