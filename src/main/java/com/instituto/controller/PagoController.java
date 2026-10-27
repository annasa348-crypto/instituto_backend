package com.instituto.controller;

import com.instituto.dto.PagoDto;
import com.instituto.dto.PagoRequest;
import com.instituto.service.PagoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoDto registrar(@Valid @RequestBody PagoRequest req, Authentication authentication) {
        return service.registrar(req, authentication.getName());
    }

    // Ej: GET /api/pagos?alumnoId=1&desde=2027-03-01&hasta=2027-03-31
    @GetMapping
    public List<PagoDto> listar(
            @RequestParam(required = false) Long alumnoId,
            @RequestParam(required = false) Long cuotaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.listar(alumnoId, cuotaId, desde, hasta);
    }
}
