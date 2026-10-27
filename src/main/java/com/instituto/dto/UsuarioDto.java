package com.instituto.dto;

import com.instituto.model.Rol;
import com.instituto.model.Usuario;

// Nunca se devuelve la contraseña (ni siquiera el hash)
public record UsuarioDto(Long id, String username, String nombre, Rol rol, boolean activo) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getUsername(), u.getNombre(), u.getRol(), u.isActivo());
    }
}
