package com.instituto.controller;

import com.instituto.dto.CuotaDto;
import com.instituto.service.CuotaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaService service;

    public CuotaController(CuotaService service) {
        this.service = service;
    }

    @GetMapping("/vencidas")
    public List<CuotaDto> vencidas() {
        return service.listarVencidas();
    }

    @GetMapping("/{id}")
    public CuotaDto obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
}
