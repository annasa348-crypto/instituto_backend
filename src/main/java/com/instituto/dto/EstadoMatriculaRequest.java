package com.instituto.dto;

import com.instituto.model.EstadoMatricula;
import jakarta.validation.constraints.NotNull;

public record EstadoMatriculaRequest(
        @NotNull(message = "El estado es obligatorio") EstadoMatricula estado) {
}
