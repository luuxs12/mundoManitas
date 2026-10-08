package com.mundomanitas.config;

import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Rol;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.repository.ModuloSistemaRepository;
import com.mundomanitas.repository.RolRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashSet;
import java.util.List;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner crearDatosIniciales(
        UsuarioRepository usuarioRepository,
        RolRepository rolRepository,
        ModuloSistemaRepository moduloSistemaRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Rol estudiante = crearRol(rolRepository, "ESTUDIANTE", "Estudiante", "Acceso del estudiante");
            Rol padre = crearRol(rolRepository, "PADRE", "Padre", "Acceso del padre de familia");
            Rol docente = crearRol(rolRepository, "DOCENTE", "Docente", "Acceso del docente");
            Rol administrador = crearRol(rolRepository, "ADMINISTRADOR", "Administrador", "Administración académica y operativa");
            Rol sistemas = crearRol(rolRepository, "SISTEMAS", "Sistemas", "Administración técnica de la plataforma");

            ModuloSistema dashboard = crearModulo(moduloSistemaRepository, "DASHBOARD", "Dashboard", "/dashboard", 1);
            ModuloSistema cursos = crearModulo(moduloSistemaRepository, "CURSOS", "Cursos", "/cursos", 2);
            ModuloSistema perfil = crearModulo(moduloSistemaRepository, "PERFIL", "Mi perfil", "/perfil", 3);

            asignarModulos(rolRepository, estudiante, List.of(dashboard, cursos, perfil));
            asignarModulos(rolRepository, padre, List.of(dashboard));
            asignarModulos(rolRepository, docente, List.of(dashboard));
            asignarModulos(rolRepository, administrador, List.of(dashboard));
            asignarModulos(rolRepository, sistemas, List.of(dashboard));

            crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Alumno Mundo Manitas", "alumno@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, padre, "Padre Mundo Manitas", "padre@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, docente, "Docente Mundo Manitas", "docente@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, administrador, "Administrador Mundo Manitas", "admin@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, sistemas, "Sistemas Mundo Manitas", "sistemas@mundomanitas.com", "123456");
        };
    }

    private Rol crearRol(RolRepository repository, String codigo, String nombre, String descripcion) {
        return repository.findByCodigoIgnoreCase(codigo).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setCodigo(codigo);
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            rol.setActivo(true);
            return repository.save(rol);
        });
    }

    private ModuloSistema crearModulo(
        ModuloSistemaRepository repository,
        String codigo,
        String nombre,
        String ruta,
        int orden
    ) {
        return repository.findByCodigoIgnoreCase(codigo).orElseGet(() -> {
            ModuloSistema modulo = new ModuloSistema();
            modulo.setCodigo(codigo);
            modulo.setNombre(nombre);
            modulo.setRuta(ruta);
            modulo.setOrden(orden);
            modulo.setActivo(true);
            return repository.save(modulo);
        });
    }

    private void asignarModulos(RolRepository repository, Rol rol, List<ModuloSistema> modulos) {
        if (!rol.getModulos().isEmpty()) {
            return;
        }

        rol.setModulos(new LinkedHashSet<>(modulos));
        repository.save(rol);
    }

    private void crearUsuario(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        Rol rol,
        String nombre,
        String email,
        String password
    ) {
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
