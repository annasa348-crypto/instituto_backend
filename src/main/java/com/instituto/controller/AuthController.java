package com.instituto.controller;

import com.instituto.dto.UsuarioDto;
import com.instituto.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * "Login" del frontend: se llama con el header Authorization (Basic).
     * Si las credenciales son correctas devuelve el usuario y su rol; si no, 401.
     */
    @GetMapping("/me")
    public UsuarioDto me(Authentication authentication) {
        return usuarioService.porUsername(authentication.getName());
    }
}
