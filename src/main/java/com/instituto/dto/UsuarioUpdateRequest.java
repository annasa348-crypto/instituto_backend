package com.instituto.dto;

import com.instituto.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// La contraseña es opcional: si no se envía, se mantiene la actual
public record UsuarioUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres") String nombre,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        @NotNull(message = "El campo activo es obligatorio") Boolean activo,
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {
}
