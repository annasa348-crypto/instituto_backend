package com.instituto.dto;

import com.instituto.model.EstadoMatricula;
import java.util.List;

public record MatriculaSituacionDto(
        Long matriculaId,
        Integer cicloLectivo,
        EstadoMatricula estado,
        List<CuotaDto> cuotas) {
}
