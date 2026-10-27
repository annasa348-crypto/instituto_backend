package com.instituto.config;

import com.instituto.model.Rol;
import com.instituto.model.Usuario;
import com.instituto.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea el primer usuario administrador si todavía no hay usuarios en el sistema. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.password:admin1234}")
    private String adminPassword;

    public DataInitializer(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode(adminPassword));
            admin.setNombre("Administrador");
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setActivo(true);
            repo.save(admin);
            log.info("Usuario inicial creado: 'admin' (contraseña definida en app.admin.password)");
        }
    }
}
