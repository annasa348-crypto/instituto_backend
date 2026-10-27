package com.instituto.controller;

import com.instituto.dto.*;
import com.instituto.model.Matricula;
import com.instituto.service.CuotaService;
import com.instituto.service.MatriculaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService service;
    private final CuotaService cuotaService;

    public MatriculaController(MatriculaService service, CuotaService cuotaService) {
        this.service = service;
        this.cuotaService = cuotaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Matricula crear(@Valid @RequestBody MatriculaRequest req) {
        return service.crear(req);
    }

    @GetMapping("/{id}")
    public Matricula obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // Matrículas de un ciclo lectivo: GET /api/matriculas?cicloId=1
    @GetMapping
    public List<Matricula> listarPorCiclo(@RequestParam Long cicloId) {
        return service.listarPorCiclo(cicloId);
    }

    @PatchMapping("/{id}/estado")
    public Matricula cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoMatriculaRequest req) {
        return service.cambiarEstado(id, req.estado());
    }

    @PostMapping("/{id}/cuotas")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CuotaDto> generarCuotas(@PathVariable Long id, @Valid @RequestBody GenerarCuotasRequest req) {
        return service.generarCuotas(id, req);
    }

    @GetMapping("/{id}/cuotas")
    public List<CuotaDto> cuotas(@PathVariable Long id) {
        service.obtener(id);
        return cuotaService.listarPorMatricula(id);
    }
}
