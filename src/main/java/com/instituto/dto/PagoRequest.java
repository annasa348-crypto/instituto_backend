package com.instituto.dto;

import com.instituto.model.MedioPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

// El usuario que registra el pago se toma del usuario autenticado, no del body
public record PagoRequest(
        @NotNull(message = "cuotaId es obligatorio") Long cuotaId,
        @NotNull(message = "El importe es obligatorio")
        @Positive(message = "El importe debe ser mayor a cero") BigDecimal importe,
        @NotNull(message = "El medio de pago es obligatorio") MedioPago medioPago) {
}
