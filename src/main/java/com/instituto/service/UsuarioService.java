package com.instituto.service;

import com.instituto.dto.UsuarioDto;
import com.instituto.dto.UsuarioRequest;
import com.instituto.dto.UsuarioUpdateRequest;
import com.instituto.exception.RecursoNoEncontradoException;
import com.instituto.exception.ReglaNegocioException;
import com.instituto.model.Rol;
import com.instituto.model.Usuario;
import com.instituto.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<UsuarioDto> listar() {
        return repo.findAll().stream().map(UsuarioDto::de).toList();
    }

    public UsuarioDto obtener(Long id) {
        return UsuarioDto.de(buscar(id));
    }

    public UsuarioDto porUsername(String username) {
        return repo.findByUsername(username)
                .map(UsuarioDto::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + username));
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest req) {
        String username = req.username().trim();
        if (repo.existsByUsername(username)) {
            throw new ReglaNegocioException("Ya existe un usuario con el nombre " + username);
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(req.password()));
        u.setNombre(req.nombre().trim());
        u.setRol(req.rol());
        u.setActivo(true);
        return UsuarioDto.de(repo.save(u));
    }

    @Transactional
    public UsuarioDto actualizar(Long id, UsuarioUpdateRequest req) {
        Usuario u = buscar(id);
        validarUltimoAdministrador(u, req.rol(), req.activo());
        u.setNombre(req.nombre().trim());
        u.setRol(req.rol());
        u.setActivo(req.activo());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(encoder.encode(req.password()));
        }
        return UsuarioDto.de(repo.save(u));
    }

    // Baja lógica: el usuario puede figurar como quien registró pagos, no se borra.
    @Transactional
    public void darDeBaja(Long id) {
        Usuario u = buscar(id);
        validarUltimoAdministrador(u, u.getRol(), false);
        u.setActivo(false);
        repo.save(u);
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    // Regla: siempre debe quedar al menos un administrador activo
    private void validarUltimoAdministrador(Usuario u, Rol nuevoRol, boolean nuevoActivo) {
        boolean eraAdminActivo = u.getRol() == Rol.ADMINISTRADOR && u.isActivo();
        boolean seguiraSiendo = nuevoRol == Rol.ADMINISTRADOR && nuevoActivo;
        if (eraAdminActivo && !seguiraSiendo && repo.countByRolAndActivoTrue(Rol.ADMINISTRADOR) <= 1) {
            throw new ReglaNegocioException("Debe quedar al menos un administrador activo en el sistema");
        }
    }
}
