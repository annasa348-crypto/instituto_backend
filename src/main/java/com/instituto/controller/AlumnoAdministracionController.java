package com.instituto.controller;

import com.instituto.dto.CuotaDto;
import com.instituto.dto.SituacionAdministrativaDto;
import com.instituto.service.CuotaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoAdministracionController {

    private final CuotaService cuotaService;

    public AlumnoAdministracionController(CuotaService cuotaService) {
        this.cuotaService = cuotaService;
    }

    @GetMapping("/{id}/cuotas")
    public List<CuotaDto> cuotas(@PathVariable Long id) {
        return cuotaService.listarPorAlumno(id);
    }

    @GetMapping("/{id}/situacion-administrativa")
    public SituacionAdministrativaDto situacion(@PathVariable Long id) {
        return cuotaService.situacion(id);
    }
}
