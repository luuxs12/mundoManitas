package com.mundomanitas.config;

import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Rol;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.entity.Video;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.EstudianteRepository;
import com.mundomanitas.repository.InscripcionRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ModuloSistemaRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.RolRepository;
import com.mundomanitas.repository.UsuarioRepository;
import com.mundomanitas.repository.VideoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner crearDatosIniciales(
        UsuarioRepository usuarioRepository,
        RolRepository rolRepository,
        ModuloSistemaRepository moduloSistemaRepository,
        ModuloRepository moduloRepository,
        VideoRepository videoRepository,
        EjercicioRepository ejercicioRepository,
        ProgresoRepository progresoRepository,
        EstudianteRepository estudianteRepository,
        InscripcionRepository inscripcionRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Rol estudiante = crearRol(rolRepository, "ESTUDIANTE", "Estudiante", "Acceso del estudiante");
            Rol padre = crearRol(rolRepository, "PADRE", "Padre", "Acceso del padre de familia");
            Rol docente = crearRol(rolRepository, "DOCENTE", "Docente", "Acceso del docente");
            Rol administrador = crearRol(rolRepository, "ADMINISTRADOR", "Administrador", "Administración académica y operativa");
            Rol sistemas = crearRol(rolRepository, "SISTEMAS", "Sistemas", "Administración técnica de la plataforma");

            ModuloSistema dashboard = crearModuloSistema(moduloSistemaRepository, "DASHBOARD", "Dashboard", "/dashboard", 1);
            ModuloSistema cursos = crearModuloSistema(moduloSistemaRepository, "CURSOS", "Cursos", "/cursos", 2);
            ModuloSistema docenteCursos = crearModuloSistema(moduloSistemaRepository, "DOCENTE_CURSOS", "Mis cursos", "/docente/cursos", 2);
            ModuloSistema adminPanel = crearModuloSistema(moduloSistemaRepository, "ADMIN_PANEL", "Panel general", "/admin", 2);
            ModuloSistema adminUsuarios = crearModuloSistema(moduloSistemaRepository, "ADMIN_USUARIOS", "Usuarios", "/admin/usuarios", 3);
            ModuloSistema adminContenido = crearModuloSistema(moduloSistemaRepository, "ADMIN_CONTENIDO", "Contenido", "/admin/contenido", 4);
            ModuloSistema perfil = crearModuloSistema(moduloSistemaRepository, "PERFIL", "Mi perfil", "/perfil", 5);

            asignarModulos(rolRepository, estudiante, List.of(dashboard, cursos, perfil));
            asignarModulos(rolRepository, padre, List.of(dashboard, perfil));
            asignarModulos(rolRepository, docente, List.of(dashboard, docenteCursos, adminContenido, perfil));
            asignarModulos(rolRepository, administrador, List.of(dashboard, adminPanel, adminUsuarios, adminContenido, perfil));
            asignarModulos(rolRepository, sistemas, List.of(dashboard, adminPanel, adminUsuarios, adminContenido, perfil));

            Usuario alumno = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Alumno Mundo Manitas", "alumno@mundomanitas.com", "123456");
            Usuario maria = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "María Fernanda Ríos", "maria@mundomanitas.com", "123456");
            Usuario diego = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Diego Salazar", "diego@mundomanitas.com", "123456");
            Usuario lucia = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Lucía Torres", "lucia@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, padre, "Padre Mundo Manitas", "padre@mundomanitas.com", "123456");
            Usuario docenteDemo = crearUsuario(usuarioRepository, passwordEncoder, docente, "Docente Mundo Manitas", "docente@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, administrador, "Administrador Mundo Manitas", "admin@mundomanitas.com", "123456");
            crearUsuario(usuarioRepository, passwordEncoder, sistemas, "Sistemas Mundo Manitas", "sistemas@mundomanitas.com", "123456");

            if (moduloRepository.count() == 0) {
                crearContenidoDemo(
                    moduloRepository,
                    videoRepository,
                    ejercicioRepository,
                    docenteDemo.getIdUsuario()
                );
            }

            // El contenido histórico sin responsable queda a cargo del docente demo.
            for (Modulo modulo : moduloRepository.findAllByOrderByOrdenAsc()) {
                if (modulo.getIdDocente() == null) {
                    modulo.setIdDocente(docenteDemo.getIdUsuario());
                    moduloRepository.save(modulo);
                }
            }

            crearPerfilEstudiante(estudianteRepository, alumno.getIdUsuario(), "Secundaria", 15, "Principiante");
            crearPerfilEstudiante(estudianteRepository, maria.getIdUsuario(), "Secundaria", 16, "Intermedio");
            crearPerfilEstudiante(estudianteRepository, diego.getIdUsuario(), "Primaria", 12, "Principiante");
            crearPerfilEstudiante(estudianteRepository, lucia.getIdUsuario(), "Secundaria", 17, "Avanzado");

            List<Modulo> modulos = moduloRepository.findAllByOrderByOrdenAsc();

            crearProgresoDemo(progresoRepository, modulos, alumno.getIdUsuario(), new int[]{100, 68, 42, 0});
            crearProgresoDemo(progresoRepository, modulos, maria.getIdUsuario(), new int[]{100, 100, 75, 30});
            crearProgresoDemo(progresoRepository, modulos, diego.getIdUsuario(), new int[]{45, 20, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, lucia.getIdUsuario(), new int[]{100, 85, 60, 100});

            inscribirEnModulos(inscripcionRepository, modulos, alumno.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, maria.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, diego.getIdUsuario(), 2);
            inscribirEnModulos(inscripcionRepository, modulos, lucia.getIdUsuario(), modulos.size());
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

    private ModuloSistema crearModuloSistema(
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
        if (rol.getModulos().addAll(modulos)) {
            repository.save(rol);
        }
    }

    private Usuario crearUsuario(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        Rol rol,
        String nombre,
        String email,
        String password
    ) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setContrasena(passwordEncoder.encode(password));
            usuario.setRol(rol);
            usuario.setActivo(true);
            return usuarioRepository.save(usuario);
        });
    }

    private void crearContenidoDemo(
        ModuloRepository moduloRepository,
        VideoRepository videoRepository,
        EjercicioRepository ejercicioRepository,
        Integer idDocente
    ) {
        crearModulo(
            moduloRepository,
            "Introducción a la programación",
            "Conceptos fundamentales de programación, algoritmos y resolución de problemas.",
            "basico",
            1,
            idDocente
        );

        crearModulo(
            moduloRepository,
            "Desarrollo Web con HTML y CSS",
            "Fundamentos para crear páginas web utilizando HTML y hojas de estilo CSS.",
            "basico",
            2,
            idDocente
        );

        Modulo java = crearModulo(
            moduloRepository,
            "Fundamentos de Java",
            "Introducción al lenguaje Java, variables, estructuras de control y programación orientada a objetos.",
            "intermedio",
            3,
            idDocente
        );

        crearModulo(
            moduloRepository,
            "Pensamiento computacional",
            "Desarrollo del pensamiento lógico mediante problemas, secuencias y algoritmos.",
            "basico",
            4,
            idDocente
        );

        crearVideo(videoRepository, java, "Introducción a Java", "https://www.youtube.com/watch?v=TzVEWpVlEH4", 480, 1);
        crearVideo(videoRepository, java, "Variables y tipos de datos", "https://www.youtube.com/watch?v=SFC7EWaJ3EQ", 720, 2);
        crearVideo(videoRepository, java, "Estructuras condicionales", "https://www.youtube.com/watch?v=HVSHPzVRJLQ", 900, 3);

        crearEjercicio(
            ejercicioRepository,
            java,
            "Identifica los tipos de datos",
            "cuestionario",
            """
            {"pregunta":"¿Qué tipo de dato se utiliza para números enteros en Java?","opciones":["String","int","boolean","double"]}
            """,
            """
            {"respuesta":"int"}
            """
        );

        crearEjercicio(
            ejercicioRepository,
            java,
            "Completa la estructura condicional",
            "completar",
            """
            {"texto":"___ (edad >= 18) { }"}
            """,
            """
            {"respuesta":"if"}
            """
        );
    }

    private Modulo crearModulo(
        ModuloRepository repository,
        String titulo,
        String descripcion,
        String nivel,
        int orden,
        Integer idDocente
    ) {
        Modulo modulo = new Modulo();
        modulo.setTitulo(titulo);
        modulo.setDescripcion(descripcion);
        modulo.setNivel(nivel);
        modulo.setOrden(orden);
        modulo.setIdDocente(idDocente);
        return repository.save(modulo);
    }

    private void crearVideo(
        VideoRepository repository,
        Modulo modulo,
        String titulo,
        String url,
        int duracion,
        int orden
    ) {
        Video video = new Video();
        video.setIdModulo(modulo.getIdModulo());
        video.setTitulo(titulo);
        video.setUrl(url);
        video.setDuracion(duracion);
        video.setOrden(orden);
        repository.save(video);
    }

    private void crearEjercicio(
        EjercicioRepository repository,
        Modulo modulo,
        String titulo,
        String tipo,
        String contenido,
        String respuestaCorrecta
    ) {
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setIdModulo(modulo.getIdModulo());
        ejercicio.setTitulo(titulo);
        ejercicio.setTipo(tipo);
        ejercicio.setContenido(contenido);
        ejercicio.setRespuestaCorrecta(respuestaCorrecta);
        repository.save(ejercicio);
    }

    private void crearPerfilEstudiante(
        EstudianteRepository repository,
        Integer idEstudiante,
        String grado,
        Integer edad,
        String nivelEstudio
    ) {
        if (repository.existsById(idEstudiante)) {
            return;
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(idEstudiante);
        estudiante.setGrado(grado);
        estudiante.setEdad(edad);
        estudiante.setNivelEstudio(nivelEstudio);
        repository.save(estudiante);
    }

    private void inscribirEnModulos(
        InscripcionRepository repository,
        List<Modulo> modulos,
        Integer idEstudiante,
        int cantidad
    ) {
        for (int i = 0; i < Math.min(cantidad, modulos.size()); i++) {
            Modulo modulo = modulos.get(i);

            if (!repository.existsByIdEstudianteAndIdModulo(
                idEstudiante,
                modulo.getIdModulo()
            )) {
                Inscripcion inscripcion = new Inscripcion();
                inscripcion.setIdEstudiante(idEstudiante);
                inscripcion.setIdModulo(modulo.getIdModulo());
                inscripcion.setEstado("ACTIVA");
                repository.save(inscripcion);
            }
        }
    }

    private void crearProgresoDemo(
        ProgresoRepository repository,
        List<Modulo> modulos,
        Integer idEstudiante,
        int[] patron
    ) {
        if (!repository.findByIdEstudiante(idEstudiante).isEmpty()) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        for (int i = 0; i < modulos.size(); i++) {
            int porcentaje = patron[Math.min(i, patron.length - 1)];

            Progreso progreso = new Progreso();
            progreso.setIdEstudiante(idEstudiante);
            progreso.setIdModulo(modulos.get(i).getIdModulo());
            progreso.setFechaInicio(ahora.minusDays(30L - (i * 5L)));
            progreso.setPorcentaje(BigDecimal.valueOf(porcentaje));

            if (porcentaje >= 100) {
                progreso.setFechaCompletado(ahora.minusDays(5));
                progreso.setCalificacion(BigDecimal.valueOf(18));
            } else if (porcentaje > 0) {
                progreso.setCalificacion(BigDecimal.valueOf(16));
            }

            repository.save(progreso);
        }
    }
}
