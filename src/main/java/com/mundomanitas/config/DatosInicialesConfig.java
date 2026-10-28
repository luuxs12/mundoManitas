package com.mundomanitas.config;

import com.mundomanitas.entity.Anuncio;
import com.mundomanitas.entity.Compra;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.Favorito;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.ModuloSistema;
import com.mundomanitas.entity.PostulacionDocente;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Resena;
import com.mundomanitas.entity.Rol;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.entity.Video;
import com.mundomanitas.repository.AnuncioRepository;
import com.mundomanitas.repository.CompraRepository;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.EstudianteRepository;
import com.mundomanitas.repository.FavoritoRepository;
import com.mundomanitas.repository.InscripcionRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ModuloSistemaRepository;
import com.mundomanitas.repository.PostulacionDocenteRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.ResenaRepository;
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
import java.util.Optional;

/**
 * Datos de demostración de Mundo Manitas.
 *
 * <p>La semilla es idempotente: se ejecuta en cada arranque y solo agrega lo
 * que falta. Así una base existente recibe los cursos y las cuentas nuevas
 * sin duplicar videos, ejercicios, progresos ni compras.</p>
 */
@Configuration
public class DatosInicialesConfig {

    private static final String PASSWORD_DEMO = "123456";

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
        PostulacionDocenteRepository postulacionRepository,
        CompraRepository compraRepository,
        FavoritoRepository favoritoRepository,
        ResenaRepository resenaRepository,
        AnuncioRepository anuncioRepository,
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
            ModuloSistema favoritos = crearModuloSistema(moduloSistemaRepository, "FAVORITOS", "Favoritos", "/cursos/favoritos", 3);
            ModuloSistema docenteCursos = crearModuloSistema(moduloSistemaRepository, "DOCENTE_CURSOS", "Mis cursos", "/docente/cursos", 2);
            ModuloSistema adminPanel = crearModuloSistema(moduloSistemaRepository, "ADMIN_PANEL", "Panel general", "/admin", 2);
            ModuloSistema adminUsuarios = crearModuloSistema(moduloSistemaRepository, "ADMIN_USUARIOS", "Usuarios", "/admin/usuarios", 3);
            ModuloSistema adminPostulaciones = crearModuloSistema(moduloSistemaRepository, "ADMIN_POSTULACIONES", "Postulaciones", "/admin/postulaciones", 3);
            ModuloSistema adminContenido = crearModuloSistema(moduloSistemaRepository, "ADMIN_CONTENIDO", "Contenido", "/admin/contenido", 4);
            ModuloSistema perfil = crearModuloSistema(moduloSistemaRepository, "PERFIL", "Mi perfil", "/perfil", 5);

            asignarModulos(rolRepository, estudiante, List.of(dashboard, cursos, favoritos, perfil));
            asignarModulos(rolRepository, padre, List.of(dashboard, perfil));
            asignarModulos(rolRepository, docente, List.of(dashboard, docenteCursos, adminContenido, perfil));
            asignarModulos(rolRepository, administrador, List.of(dashboard, adminPanel, adminUsuarios, adminPostulaciones, adminContenido, perfil));
            asignarModulos(rolRepository, sistemas, List.of(dashboard, adminPanel, adminUsuarios, adminPostulaciones, adminContenido, perfil));

            // ------------------------------------------------------------------
            // Cuentas demo
            // ------------------------------------------------------------------

            Usuario alumno = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Alumno Mundo Manitas", "alumno@mundomanitas.com");
            Usuario maria = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "María Fernanda Ríos", "maria@mundomanitas.com");
            Usuario diego = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Diego Salazar", "diego@mundomanitas.com");
            Usuario lucia = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Lucía Torres", "lucia@mundomanitas.com");
            Usuario sofia = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Sofía Ramírez", "sofia@mundomanitas.com");
            Usuario mateo = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Mateo Huamán", "mateo@mundomanitas.com");
            Usuario valeria = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Valeria Ccahuana", "valeria@mundomanitas.com");
            Usuario renzo = crearUsuario(usuarioRepository, passwordEncoder, estudiante, "Renzo Aguilar", "renzo@mundomanitas.com");

            crearUsuario(usuarioRepository, passwordEncoder, padre, "Padre Mundo Manitas", "padre@mundomanitas.com");
            crearUsuario(usuarioRepository, passwordEncoder, padre, "Rosa Elena Huamán", "rosa@mundomanitas.com");

            Usuario docenteDemo = crearUsuario(usuarioRepository, passwordEncoder, docente, "Docente Mundo Manitas", "docente@mundomanitas.com");
            Usuario carlos = crearUsuario(usuarioRepository, passwordEncoder, docente, "Carlos Mendoza", "carlos@mundomanitas.com");
            Usuario elena = crearUsuario(usuarioRepository, passwordEncoder, docente, "Elena Vargas", "elena@mundomanitas.com");
            Usuario jorge = crearUsuario(usuarioRepository, passwordEncoder, docente, "Jorge Paredes", "jorge@mundomanitas.com");

            crearUsuario(usuarioRepository, passwordEncoder, administrador, "Administrador Mundo Manitas", "admin@mundomanitas.com");
            crearUsuario(usuarioRepository, passwordEncoder, sistemas, "Sistemas Mundo Manitas", "sistemas@mundomanitas.com");

            crearPostulacionDemo(postulacionRepository);

            // ------------------------------------------------------------------
            // Contenido educativo
            // ------------------------------------------------------------------

            crearContenidoDemo(
                moduloRepository,
                videoRepository,
                ejercicioRepository,
                docenteDemo.getIdUsuario(),
                carlos.getIdUsuario(),
                elena.getIdUsuario(),
                jorge.getIdUsuario()
            );

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
            crearPerfilEstudiante(estudianteRepository, sofia.getIdUsuario(), "Secundaria", 16, "Intermedio");
            crearPerfilEstudiante(estudianteRepository, mateo.getIdUsuario(), "Primaria", 13, "Principiante");
            crearPerfilEstudiante(estudianteRepository, valeria.getIdUsuario(), "Secundaria", 18, "Avanzado");
            crearPerfilEstudiante(estudianteRepository, renzo.getIdUsuario(), "Secundaria", 14, "Principiante");

            List<Modulo> modulos = moduloRepository.findAllByOrderByOrdenAsc();

            crearProgresoDemo(progresoRepository, modulos, alumno.getIdUsuario(), new int[]{100, 68, 42, 0, 30, 55, 0, 0, 0, 0, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, maria.getIdUsuario(), new int[]{100, 100, 75, 30, 60, 40, 20, 0, 0, 0, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, diego.getIdUsuario(), new int[]{45, 20, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, lucia.getIdUsuario(), new int[]{100, 85, 60, 100, 90, 70, 50, 40, 30, 20, 10, 0});
            crearProgresoDemo(progresoRepository, modulos, sofia.getIdUsuario(), new int[]{100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, mateo.getIdUsuario(), new int[]{30, 40, 0, 20, 10, 0, 0, 0, 0, 0, 0, 0});
            crearProgresoDemo(progresoRepository, modulos, valeria.getIdUsuario(), new int[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100});
            crearProgresoDemo(progresoRepository, modulos, renzo.getIdUsuario(), new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

            inscribirEnModulos(inscripcionRepository, modulos, alumno.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, maria.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, diego.getIdUsuario(), 6);
            inscribirEnModulos(inscripcionRepository, modulos, lucia.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, sofia.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, mateo.getIdUsuario(), 5);
            inscribirEnModulos(inscripcionRepository, modulos, valeria.getIdUsuario(), modulos.size());
            inscribirEnModulos(inscripcionRepository, modulos, renzo.getIdUsuario(), 3);

            crearComprasDemo(compraRepository, modulos, alumno.getIdUsuario(), sofia.getIdUsuario(), valeria.getIdUsuario());
            crearComunidadDemo(
                favoritoRepository,
                resenaRepository,
                anuncioRepository,
                modulos,
                alumno.getIdUsuario(),
                maria.getIdUsuario(),
                diego.getIdUsuario(),
                lucia.getIdUsuario(),
                sofia.getIdUsuario(),
                docenteDemo.getIdUsuario(),
                carlos.getIdUsuario(),
                elena.getIdUsuario(),
                jorge.getIdUsuario()
            );
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
        String email
    ) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setContrasena(passwordEncoder.encode(PASSWORD_DEMO));
            usuario.setRol(rol);
            usuario.setActivo(true);
            return usuarioRepository.save(usuario);
        });
    }

    // ------------------------------------------------------------------
    // Cursos, videos y ejercicios
    // ------------------------------------------------------------------

    private void crearContenidoDemo(
        ModuloRepository moduloRepository,
        VideoRepository videoRepository,
        EjercicioRepository ejercicioRepository,
        Integer docenteDemo,
        Integer carlos,
        Integer elena,
        Integer jorge
    ) {
        Modulo intro = crearModuloSiFalta(
            moduloRepository, "Introducción a la programación",
            "Conceptos fundamentales de programación, algoritmos y resolución de problemas.",
            "basico", 1, docenteDemo, false, BigDecimal.ZERO
        );
        Modulo web = crearModuloSiFalta(
            moduloRepository, "Desarrollo Web con HTML y CSS",
            "Fundamentos para crear páginas web utilizando HTML y hojas de estilo CSS.",
            "basico", 2, docenteDemo, false, BigDecimal.ZERO
        );
        Modulo java = crearModuloSiFalta(
            moduloRepository, "Fundamentos de Java",
            "Introducción al lenguaje Java, variables, estructuras de control y programación orientada a objetos.",
            "intermedio", 3, docenteDemo, true, new BigDecimal("49.90")
        );
        Modulo pensamiento = crearModuloSiFalta(
            moduloRepository, "Pensamiento computacional",
            "Desarrollo del pensamiento lógico mediante problemas, secuencias y algoritmos.",
            "basico", 4, docenteDemo, false, BigDecimal.ZERO
        );
        Modulo python = crearModuloSiFalta(
            moduloRepository, "Python para principiantes",
            "Primeros pasos con Python: variables, listas, funciones y proyectos sencillos.",
            "basico", 5, carlos, false, BigDecimal.ZERO
        );
        Modulo sql = crearModuloSiFalta(
            moduloRepository, "Bases de datos con SQL",
            "Diseño de tablas, consultas, filtros y relaciones para organizar información.",
            "intermedio", 6, carlos, false, BigDecimal.ZERO
        );
        Modulo ux = crearModuloSiFalta(
            moduloRepository, "Diseño UX accesible",
            "Principios de experiencia de usuario y accesibilidad para productos digitales inclusivos.",
            "intermedio", 7, elena, false, BigDecimal.ZERO
        );
        Modulo moviles = crearModuloSiFalta(
            moduloRepository, "Desarrollo de apps móviles",
            "Crea aplicaciones móviles accesibles, desde el prototipo hasta la publicación.",
            "avanzado", 8, elena, true, new BigDecimal("69.90")
        );
        Modulo seguridad = crearModuloSiFalta(
            moduloRepository, "Seguridad informática básica",
            "Contraseñas seguras, phishing, respaldo de datos y buenas prácticas digitales.",
            "intermedio", 9, jorge, false, BigDecimal.ZERO
        );
        Modulo ia = crearModuloSiFalta(
            moduloRepository, "Inteligencia artificial aplicada",
            "Introducción práctica a los modelos de IA, sus usos y sus límites éticos.",
            "avanzado", 10, jorge, true, new BigDecimal("79.90")
        );
        Modulo lsp = crearModuloSiFalta(
            moduloRepository, "Lengua de Señas y tecnología",
            "Vocabulario técnico en Lengua de Señas Peruana para entornos digitales.",
            "basico", 11, elena, false, BigDecimal.ZERO
        );
        Modulo emprendimiento = crearModuloSiFalta(
            moduloRepository, "Emprendimiento digital",
            "Convierte una idea en un proyecto digital: clientes, propuesta de valor y presencia web.",
            "intermedio", 12, carlos, false, BigDecimal.ZERO
        );

        crearVideo(videoRepository, intro, "¿Qué es un algoritmo?", "https://www.youtube.com/watch?v=8mAITcNt710", 600, 1);
        crearVideo(videoRepository, intro, "Primer programa paso a paso", "https://www.youtube.com/watch?v=zOjov-2OZ0E", 720, 2);
        crearEjercicio(ejercicioRepository, intro, "Ordena los pasos de un algoritmo", "completar",
            """
            {"texto":"Un algoritmo debe tener inicio, ___ y fin."}
            """,
            """
            {"respuesta":"proceso"}
            """);

        crearVideo(videoRepository, web, "Estructura de una página HTML", "https://www.youtube.com/watch?v=UB1O30fR-EE", 900, 1);
        crearVideo(videoRepository, web, "Estilos con CSS", "https://www.youtube.com/watch?v=yfoY53QXEnI", 840, 2);
        crearEjercicio(ejercicioRepository, web, "Etiqueta para un título principal", "cuestionario",
            """
            {"pregunta":"¿Qué etiqueta representa el título principal de una página?","opciones":["<p>","<h1>","<div>","<span>"]}
            """,
            """
            {"respuesta":"<h1>"}
            """);

        crearVideo(videoRepository, java, "Introducción a Java", "https://www.youtube.com/watch?v=TzVEWpVlEH4", 480, 1);
        crearVideo(videoRepository, java, "Variables y tipos de datos", "https://www.youtube.com/watch?v=SFC7EWaJ3EQ", 720, 2);
        crearVideo(videoRepository, java, "Estructuras condicionales", "https://www.youtube.com/watch?v=HVSHPzVRJLQ", 900, 3);
        crearEjercicio(ejercicioRepository, java, "Identifica los tipos de datos", "cuestionario",
            """
            {"pregunta":"¿Qué tipo de dato se utiliza para números enteros en Java?","opciones":["String","int","boolean","double"]}
            """,
            """
            {"respuesta":"int"}
            """);
        crearEjercicio(ejercicioRepository, java, "Completa la estructura condicional", "completar",
            """
            {"texto":"___ (edad >= 18) { }"}
            """,
            """
            {"respuesta":"if"}
            """);

        crearVideo(videoRepository, pensamiento, "Descomponer un problema", "https://www.youtube.com/watch?v=Y1VwQ6X8Q1Q", 540, 1);
        crearVideo(videoRepository, pensamiento, "Patrones y secuencias", "https://www.youtube.com/watch?v=5K6N6u9d3xE", 600, 2);
        crearEjercicio(ejercicioRepository, pensamiento, "Completa la secuencia", "completar",
            """
            {"texto":"2, 4, 6, 8, ___"}
            """,
            """
            {"respuesta":"10"}
            """);

        crearVideo(videoRepository, python, "Tu primer programa en Python", "https://www.youtube.com/watch?v=rfscVS0vtbw", 780, 1);
        crearVideo(videoRepository, python, "Listas y funciones", "https://www.youtube.com/watch?v=W8KRzm-HUcc", 900, 2);
        crearEjercicio(ejercicioRepository, python, "Imprime un saludo", "completar",
            """
            {"texto":"___(\"Hola Mundo\")"}
            """,
            """
            {"respuesta":"print"}
            """);

        crearVideo(videoRepository, sql, "Tablas y registros", "https://www.youtube.com/watch?v=HXV3zeQKqGY", 960, 1);
        crearVideo(videoRepository, sql, "Consultas con SELECT", "https://www.youtube.com/watch?v=7S_tz1z_5bA", 840, 2);
        crearEjercicio(ejercicioRepository, sql, "Selecciona todos los registros", "completar",
            """
            {"texto":"SELECT * ___ estudiantes;"}
            """,
            """
            {"respuesta":"FROM"}
            """);

        crearVideo(videoRepository, ux, "Qué es la accesibilidad digital", "https://www.youtube.com/watch?v=3f31oufqFSM", 600, 1);
        crearVideo(videoRepository, ux, "Diseñar para todas las personas", "https://www.youtube.com/watch?v=20Qr1L1s1xQ", 720, 2);
        crearEjercicio(ejercicioRepository, ux, "Contraste y legibilidad", "cuestionario",
            """
            {"pregunta":"¿Qué mejora la legibilidad de un texto digital?","opciones":["Bajo contraste","Texto muy pequeño","Buen contraste","Muchos colores"]}
            """,
            """
            {"respuesta":"Buen contraste"}
            """);

        crearVideo(videoRepository, moviles, "Del prototipo a la app", "https://www.youtube.com/watch?v=0-S5a0eXPoc", 900, 1);
        crearVideo(videoRepository, moviles, "Accesibilidad en móviles", "https://www.youtube.com/watch?v=1BfV0m1g1xQ", 780, 2);
        crearEjercicio(ejercicioRepository, moviles, "Buen tamaño de área táctil", "cuestionario",
            """
            {"pregunta":"¿Qué tamaño mínimo se recomienda para un botón táctil?","opciones":["12 px","24 px","48 px","8 px"]}
            """,
            """
            {"respuesta":"48 px"}
            """);

        crearVideo(videoRepository, seguridad, "Contraseñas seguras", "https://www.youtube.com/watch?v=3NjQ9b3pgIg", 540, 1);
        crearVideo(videoRepository, seguridad, "Cómo reconocer un phishing", "https://www.youtube.com/watch?v=XBkzBrXlle0", 660, 2);
        crearEjercicio(ejercicioRepository, seguridad, "Detecta el correo sospechoso", "cuestionario",
            """
            {"pregunta":"¿Qué señal indica un posible phishing?","opciones":["Remitente conocido","Urgencia y enlaces extraños","Firma de la empresa","Asunto claro"]}
            """,
            """
            {"respuesta":"Urgencia y enlaces extraños"}
            """);

        crearVideo(videoRepository, ia, "Qué puede y qué no puede la IA", "https://www.youtube.com/watch?v=2ePf9rue1Ao", 840, 1);
        crearVideo(videoRepository, ia, "IA con propósito social", "https://www.youtube.com/watch?v=1BfV0m1g1xQ", 720, 2);
        crearEjercicio(ejercicioRepository, ia, "Uso responsable de la IA", "cuestionario",
            """
            {"pregunta":"¿Qué práctica es responsable al usar IA?","opciones":["Copiar sin revisar","Verificar la información","Ocultar las fuentes","Compartir datos personales"]}
            """,
            """
            {"respuesta":"Verificar la información"}
            """);

        crearVideo(videoRepository, lsp, "Señas para saludar y presentarse", "https://www.youtube.com/watch?v=1BfV0m1g1xQ", 600, 1);
        crearVideo(videoRepository, lsp, "Vocabulario tecnológico en LSP", "https://www.youtube.com/watch?v=20Qr1L1s1xQ", 720, 2);
        crearEjercicio(ejercicioRepository, lsp, "Seña de saludo", "completar",
            """
            {"texto":"La seña de ___ se usa al encontrarse con otra persona."}
            """,
            """
            {"respuesta":"hola"}
            """);

        crearVideo(videoRepository, emprendimiento, "De la idea al cliente", "https://www.youtube.com/watch?v=1BfV0m1g1xQ", 720, 1);
        crearVideo(videoRepository, emprendimiento, "Presencia digital responsable", "https://www.youtube.com/watch?v=20Qr1L1s1xQ", 660, 2);
        crearEjercicio(ejercicioRepository, emprendimiento, "Propuesta de valor", "completar",
            """
            {"texto":"Una propuesta de valor explica por qué tu producto es ___ para el cliente."}
            """,
            """
            {"respuesta":"útil"}
            """);
    }

    private Modulo crearModuloSiFalta(
        ModuloRepository repository,
        String titulo,
        String descripcion,
        String nivel,
        int orden,
        Integer idDocente,
        boolean premium,
        BigDecimal precio
    ) {
        Optional<Modulo> existente = repository.findAllByOrderByOrdenAsc().stream()
            .filter(modulo -> modulo.getTitulo().equalsIgnoreCase(titulo))
            .findFirst();

        if (existente.isPresent()) {
            Modulo modulo = existente.get();

            if (modulo.getIdDocente() == null) {
                modulo.setIdDocente(idDocente);
                return repository.save(modulo);
            }

            return modulo;
        }

        Modulo modulo = new Modulo();
        modulo.setTitulo(titulo);
        modulo.setDescripcion(descripcion);
        modulo.setNivel(nivel);
        modulo.setOrden(orden);
        modulo.setIdDocente(idDocente);
        modulo.setPremium(premium);
        modulo.setPrecio(premium ? precio : BigDecimal.ZERO);

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
        if (repository.existsByIdModuloAndTituloIgnoreCase(modulo.getIdModulo(), titulo)) {
            return;
        }

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
        if (repository.existsByIdModuloAndTituloIgnoreCase(modulo.getIdModulo(), titulo)) {
            return;
        }

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setIdModulo(modulo.getIdModulo());
        ejercicio.setTitulo(titulo);
        ejercicio.setTipo(tipo);
        ejercicio.setContenido(contenido);
        ejercicio.setRespuestaCorrecta(respuestaCorrecta);
        repository.save(ejercicio);
    }

    // ------------------------------------------------------------------
    // Comunidad del aula
    // ------------------------------------------------------------------

    private void crearComprasDemo(
        CompraRepository repository,
        List<Modulo> modulos,
        Integer alumno,
        Integer sofia,
        Integer valeria
    ) {
        comprar(repository, modulos, alumno, "Fundamentos de Java");
        comprar(repository, modulos, sofia, "Fundamentos de Java");
        comprar(repository, modulos, valeria, "Fundamentos de Java");
        comprar(repository, modulos, valeria, "Desarrollo de apps móviles");
        comprar(repository, modulos, valeria, "Inteligencia artificial aplicada");
    }

    private void comprar(
        CompraRepository repository,
        List<Modulo> modulos,
        Integer idEstudiante,
        String tituloCurso
    ) {
        Modulo modulo = modulos.stream()
            .filter(curso -> curso.getTitulo().equalsIgnoreCase(tituloCurso))
            .findFirst()
            .orElse(null);

        if (modulo == null || repository.existsByIdEstudianteAndIdModulo(
            idEstudiante,
            modulo.getIdModulo()
        )) {
            return;
        }

        Compra compra = new Compra();
        compra.setIdEstudiante(idEstudiante);
        compra.setIdModulo(modulo.getIdModulo());
        compra.setMonto(modulo.getPrecio());
        compra.setEstado("PAGADA");
        compra.setMetodoPago("SIMULADO");
        compra.setReferencia("MM-DEMO-" + idEstudiante + "-" + modulo.getIdModulo());
        compra.setFechaCompra(LocalDateTime.now().minusDays(12));
        repository.save(compra);
    }

    private void crearComunidadDemo(
        FavoritoRepository favoritoRepository,
        ResenaRepository resenaRepository,
        AnuncioRepository anuncioRepository,
        List<Modulo> modulos,
        Integer alumno,
        Integer maria,
        Integer diego,
        Integer lucia,
        Integer sofia,
        Integer docenteDemo,
        Integer carlos,
        Integer elena,
        Integer jorge
    ) {
        favorito(favoritoRepository, modulos, alumno, "Desarrollo Web con HTML y CSS");
        favorito(favoritoRepository, modulos, alumno, "Python para principiantes");
        favorito(favoritoRepository, modulos, alumno, "Desarrollo de apps móviles");
        favorito(favoritoRepository, modulos, maria, "Pensamiento computacional");
        favorito(favoritoRepository, modulos, maria, "Diseño UX accesible");
        favorito(favoritoRepository, modulos, sofia, "Seguridad informática básica");
        favorito(favoritoRepository, modulos, sofia, "Inteligencia artificial aplicada");

        resena(resenaRepository, modulos, alumno, "Introducción a la programación", 5,
            "Las explicaciones son muy claras y los ejemplos ayudan a perder el miedo a programar.");
        resena(resenaRepository, modulos, maria, "Introducción a la programación", 4,
            "Me gustó el ritmo del curso. Los ejercicios refuerzan lo visto en los videos.");
        resena(resenaRepository, modulos, lucia, "Desarrollo Web con HTML y CSS", 5,
            "El contenido es visual y accesible. Pude armar mi primera página sin dificultad.");
        resena(resenaRepository, modulos, diego, "Introducción a la programación", 4,
            "Los ejemplos son sencillos y se entienden bien.");
        resena(resenaRepository, modulos, sofia, "Fundamentos de Java", 5,
            "El curso premium vale la pena: los ejercicios y el certificado motivan a terminar.");
        resena(resenaRepository, modulos, maria, "Diseño UX accesible", 5,
            "Ahora entiendo por qué el contraste y las etiquetas importan en una interfaz.");

        anuncio(anuncioRepository, modulos, docenteDemo, "Introducción a la programación",
            "Bienvenida al curso",
            "Revisa el video de bienvenida y completa el primer ejercicio antes del viernes. "
                + "Si tienes dudas, escribe en el aula virtual.");
        anuncio(anuncioRepository, modulos, docenteDemo, "Desarrollo Web con HTML y CSS",
            "Material complementario",
            "Subí una guía de etiquetas HTML y ejemplos de CSS. Puedes descargarla y usarla "
                + "como referencia para tu proyecto final.");
        anuncio(anuncioRepository, modulos, docenteDemo, "Fundamentos de Java",
            "Entrega del primer ejercicio",
            "La primera entrega se revisa el jueves. Recuerda marcar los videos como completados "
                + "para que tu progreso se actualice.");
        anuncio(anuncioRepository, modulos, carlos, "Python para principiantes",
            "Nuevo laboratorio práctico",
            "Esta semana trabajaremos con listas y funciones. Trae tu computadora o usa el "
                + "entorno en línea que compartimos en clase.");
        anuncio(anuncioRepository, modulos, elena, "Diseño UX accesible",
            "Revisión de prototipos",
            "Publicaré comentarios de sus prototipos el lunes. Revisen el contraste y el tamaño "
                + "de los botones antes de enviarlos.");
        anuncio(anuncioRepository, modulos, jorge, "Seguridad informática básica",
            "Simulacro de phishing",
            "El miércoles haremos un simulacro. Recuerda: nunca compartas tu contraseña ni "
                + "hagas clic en enlaces sospechosos.");
    }

    private void favorito(
        FavoritoRepository repository,
        List<Modulo> modulos,
        Integer idEstudiante,
        String tituloCurso
    ) {
        Modulo modulo = buscarModulo(modulos, tituloCurso);

        if (modulo == null || repository.existsByIdEstudianteAndIdModulo(
            idEstudiante,
            modulo.getIdModulo()
        )) {
            return;
        }

        Favorito favorito = new Favorito();
        favorito.setIdEstudiante(idEstudiante);
        favorito.setIdModulo(modulo.getIdModulo());
        favorito.setFechaCreacion(LocalDateTime.now().minusDays(4));
        repository.save(favorito);
    }

    private void resena(
        ResenaRepository repository,
        List<Modulo> modulos,
        Integer idEstudiante,
        String tituloCurso,
        int calificacion,
        String comentario
    ) {
        Modulo modulo = buscarModulo(modulos, tituloCurso);

        if (modulo == null || repository.existsByIdEstudianteAndIdModulo(
            idEstudiante,
            modulo.getIdModulo()
        )) {
            return;
        }

        Resena resena = new Resena();
        resena.setIdEstudiante(idEstudiante);
        resena.setIdModulo(modulo.getIdModulo());
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setFechaCreacion(LocalDateTime.now().minusDays(6));
        repository.save(resena);
    }

    private void anuncio(
        AnuncioRepository repository,
        List<Modulo> modulos,
        Integer idDocente,
        String tituloCurso,
        String titulo,
        String mensaje
    ) {
        Modulo modulo = buscarModulo(modulos, tituloCurso);

        if (modulo == null || repository.existsByIdModuloAndTituloIgnoreCase(
            modulo.getIdModulo(),
            titulo
        )) {
            return;
        }

        Anuncio anuncio = new Anuncio();
        anuncio.setIdModulo(modulo.getIdModulo());
        anuncio.setIdDocente(idDocente);
        anuncio.setTitulo(titulo);
        anuncio.setMensaje(mensaje);
        anuncio.setFechaPublicacion(LocalDateTime.now().minusDays(2));
        anuncio.setActivo(true);
        repository.save(anuncio);
    }

    private Modulo buscarModulo(List<Modulo> modulos, String titulo) {
        return modulos.stream()
            .filter(modulo -> modulo.getTitulo().equalsIgnoreCase(titulo))
            .findFirst()
            .orElse(null);
    }

    // ------------------------------------------------------------------
    // Perfiles, progreso e inscripciones
    // ------------------------------------------------------------------

    /**
     * Postulación de ejemplo para que el administrador vea el flujo de
     * revisión desde el primer arranque. No crea ninguna cuenta.
     */
    private void crearPostulacionDemo(PostulacionDocenteRepository repository) {
        String email = "ana.quispe@ejemplo.com";

        if (repository.findFirstByEmailIgnoreCaseOrderByFechaPostulacionDesc(email).isPresent()) {
            return;
        }

        PostulacionDocente postulacion = new PostulacionDocente();
        postulacion.setNombre("Ana Lucía Quispe Ramos");
        postulacion.setEmail(email);
        postulacion.setTelefono("987654321");
        postulacion.setNivelLsp("C1");
        postulacion.setCertificacionLsp("Certificación de LSP nivel C1 - INSOR Perú");
        postulacion.setAniosExperiencia(4);
        postulacion.setAreaTi("Desarrollo web y accesibilidad");
        postulacion.setPortafolio("https://portafolio.ejemplo.com/ana-quispe");
        postulacion.setMotivacion(
            "Soy intérprete de Lengua de Señas Peruana y desarrolladora web. "
                + "Quiero crear material visual accesible para que más estudiantes "
                + "sordos aprendan programación sin depender del audio."
        );
        postulacion.setEstado(EstadoPostulacion.PENDIENTE);
        postulacion.setFechaPostulacion(LocalDateTime.now().minusDays(2));

        repository.save(postulacion);
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
        LocalDateTime ahora = LocalDateTime.now();

        for (int i = 0; i < modulos.size(); i++) {
            Modulo modulo = modulos.get(i);

            if (repository.findByIdEstudianteAndIdModulo(
                idEstudiante,
                modulo.getIdModulo()
            ).isPresent()) {
                continue;
            }

            int porcentaje = patron[Math.min(i, patron.length - 1)];

            Progreso progreso = new Progreso();
            progreso.setIdEstudiante(idEstudiante);
            progreso.setIdModulo(modulo.getIdModulo());
            progreso.setFechaInicio(ahora.minusDays(30L - (i * 2L)));
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
