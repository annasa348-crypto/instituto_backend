package com.instituto.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GenerarCuotasRequest(
        @NotNull(message = "La cantidad de cuotas es obligatoria")
        @Min(value = 1, message = "Debe generarse al menos 1 cuota")
        @Max(value = 12, message = "No se pueden generar más de 12 cuotas") Integer cantidad,
        @NotNull(message = "El importe de la cuota es obligatorio")
        @Positive(message = "El importe debe ser mayor a cero") BigDecimal importeCuota,
        @NotNull(message = "El primer vencimiento es obligatorio") LocalDate primerVencimiento) {
}
