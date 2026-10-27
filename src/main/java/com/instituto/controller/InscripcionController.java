package com.instituto.controller;

import com.instituto.model.Inscripcion;
import com.instituto.service.InscripcionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    public record InscripcionRequest(
            @NotNull(message = "alumnoId es obligatorio") Long alumnoId,
            @NotNull(message = "cursoId es obligatorio") Long cursoId) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inscripcion inscribir(@Valid @RequestBody InscripcionRequest req) {
        return service.inscribir(req.alumnoId(), req.cursoId());
    }

    @GetMapping("/curso/{cursoId}")
    public List<Inscripcion> porCurso(@PathVariable Long cursoId) {
        return service.listarPorCurso(cursoId);
    }
}
