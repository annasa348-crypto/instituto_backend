package com.instituto.controller;

import com.instituto.model.Alumno;
import com.instituto.service.AlumnoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoService service;

    public AlumnoController(AlumnoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Alumno> listar(@RequestParam(required = false) String q) {
        return service.listar(q);
    }

    @GetMapping("/{id}")
    public Alumno obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alumno crear(@Valid @RequestBody Alumno alumno) {
        return service.crear(alumno);
    }

    @PutMapping("/{id}")
    public Alumno actualizar(@PathVariable Long id, @Valid @RequestBody Alumno alumno) {
        return service.actualizar(id, alumno);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}
