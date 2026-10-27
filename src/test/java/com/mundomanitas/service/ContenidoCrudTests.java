package com.mundomanitas.service;

import com.mundomanitas.dto.api.EjercicioDto;
import com.mundomanitas.dto.api.EjercicioRequest;
import com.mundomanitas.dto.api.ModuloDto;
import com.mundomanitas.dto.api.ModuloRequest;
import com.mundomanitas.dto.api.VideoDto;
import com.mundomanitas.dto.api.VideoRequest;
import com.mundomanitas.dto.view.KpiView;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Video;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.UsuarioRepository;
import com.mundomanitas.repository.VideoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del CRUD de contenido: creación, edición y borrado de módulos,
 * videos y ejercicios, además del control de propiedad del docente y las
 * rutas de los indicadores de los paneles.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ContenidoCrudTests {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private PanelService panelService;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Modulo moduloJava() {
        return moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(modulo -> "Fundamentos de Java".equals(modulo.getTitulo()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("El módulo demo no existe"));
    }

    private Video videoJava() {
        return videoRepository.findAll().stream()
            .filter(video -> "Introducción a Java".equals(video.getTitulo()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("El video demo no existe"));
    }

    private Ejercicio ejercicioJava() {
        return ejercicioRepository.findAll().stream()
            .filter(ejercicio -> "Identifica los tipos de datos".equals(ejercicio.getTitulo()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("El ejercicio demo no existe"));
    }

    private Integer idAdministrador() {
        return usuarioRepository.findByEmailIgnoreCase("admin@mundomanitas.com")
            .orElseThrow(() -> new IllegalStateException("El administrador demo no existe"))
            .getIdUsuario();
    }

    // ------------------------------------------------------------------
    // Módulos
    // ------------------------------------------------------------------

    @Test
    void elDocenteActualizaSuModuloYElCambioPersiste() {
        Modulo modulo = moduloJava();

        ModuloDto actualizado = cursoService.actualizarModulo(
            modulo.getIdModulo(),
            new ModuloRequest(
                "Fundamentos de Java II",
                "Contenido actualizado desde el panel",
                "avanzado",
                12
            ),
            modulo.getIdDocente()
        );

        assertEquals("Fundamentos de Java II", actualizado.titulo());
        assertEquals("avanzado", actualizado.nivel());
        assertEquals(12, actualizado.orden());

        Modulo enBase = moduloRepository.findById(modulo.getIdModulo()).orElseThrow();
        assertEquals("Fundamentos de Java II", enBase.getTitulo());
        assertEquals("Contenido actualizado desde el panel", enBase.getDescripcion());
        assertEquals("avanzado", enBase.getNivel());
    }

    @Test
    void elDocenteNoPuedeEditarUnModuloAjeno() {
        Modulo modulo = moduloJava();

        assertThrows(ReglaNegocioException.class, () -> cursoService.actualizarModulo(
            modulo.getIdModulo(),
            new ModuloRequest("Intento ajeno", "No debería guardarse", "basico", 1),
            idAdministrador()
        ));

        assertEquals("Fundamentos de Java", moduloRepository.findById(modulo.getIdModulo())
            .orElseThrow()
            .getTitulo());
    }

    @Test
    void laAdministracionPuedeEditarCualquierModulo() {
        Modulo modulo = moduloJava();

        ModuloDto actualizado = cursoService.actualizarModulo(
            modulo.getIdModulo(),
            new ModuloRequest("Fundamentos de Java (admin)", "Edición administrativa", "intermedio", 3),
            null
        );

        assertEquals("Fundamentos de Java (admin)", actualizado.titulo());
    }

    @Test
    void noSePuedeDuplicarElTituloDeUnModulo() {
        Modulo java = moduloJava();
        Modulo otro = moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(modulo -> !modulo.getIdModulo().equals(java.getIdModulo()))
            .findFirst()
            .orElseThrow();

        assertThrows(ReglaNegocioException.class, () -> cursoService.actualizarModulo(
            otro.getIdModulo(),
            new ModuloRequest(java.getTitulo(), otro.getDescripcion(), otro.getNivel(), otro.getOrden()),
            null
        ));
    }

    @Test
    void eliminarUnModuloLoQuitaDeLaBase() {
        ModuloDto creado = cursoService.crearModulo(
            new ModuloRequest("Módulo de prueba", "Se elimina al final", "basico", 99),
            null
        );

        cursoService.eliminarModulo(creado.idModulo());

        assertFalse(moduloRepository.findById(creado.idModulo()).isPresent());
    }

    @Test
    void elDocenteNoPuedeEliminarUnModuloAjeno() {
        Modulo modulo = moduloJava();

        assertThrows(ReglaNegocioException.class, () ->
            cursoService.verificarPropiedad(modulo.getIdModulo(), idAdministrador())
        );
    }

    // ------------------------------------------------------------------
    // Videos
    // ------------------------------------------------------------------

    @Test
    void actualizarUnVideoCambiaSusDatosEnLaBase() {
        Video video = videoJava();

        VideoDto actualizado = cursoService.actualizarVideo(
            video.getIdVideo(),
            new VideoRequest(
                video.getIdModulo(),
                "Introducción a Java (v2)",
                "https://www.youtube.com/watch?v=actualizado",
                500,
                4
            ),
            moduloJava().getIdDocente()
        );

        assertEquals("Introducción a Java (v2)", actualizado.titulo());
        assertEquals(500, actualizado.duracion());
        assertEquals(4, actualizado.orden());

        Video enBase = videoRepository.findById(video.getIdVideo()).orElseThrow();
        assertEquals("Introducción a Java (v2)", enBase.getTitulo());
        assertEquals("https://www.youtube.com/watch?v=actualizado", enBase.getUrl());
        assertEquals(500, enBase.getDuracion());
    }

    @Test
    void elDocenteNoPuedeEditarUnVideoAjeno() {
        Video video = videoJava();

        assertThrows(ReglaNegocioException.class, () -> cursoService.actualizarVideo(
            video.getIdVideo(),
            new VideoRequest(video.getIdModulo(), "Intento ajeno", video.getUrl(), 10, 1),
            idAdministrador()
        ));
    }

    @Test
    void crearEditarYEliminarUnVideoPersisteEnLaBase() {
        Modulo modulo = moduloJava();

        VideoDto creado = cursoService.crearVideo(
            new VideoRequest(modulo.getIdModulo(), "Video de prueba", "https://ejemplo.com/prueba", 120, 9),
            modulo.getIdDocente()
        );

        VideoDto editado = cursoService.actualizarVideo(
            creado.idVideo(),
            new VideoRequest(modulo.getIdModulo(), "Video de prueba editado", "https://ejemplo.com/edicion", 150, 8),
            modulo.getIdDocente()
        );

        assertEquals("Video de prueba editado", editado.titulo());
        assertEquals(150, videoRepository.findById(editado.idVideo()).orElseThrow().getDuracion());

        cursoService.eliminarVideo(editado.idVideo(), modulo.getIdDocente());

        assertFalse(videoRepository.findById(editado.idVideo()).isPresent());
    }

    // ------------------------------------------------------------------
    // Ejercicios
    // ------------------------------------------------------------------

    @Test
    void actualizarUnEjercicioConservaLaRespuestaCorrecta() {
        Ejercicio ejercicio = ejercicioJava();

        EjercicioDto actualizado = cursoService.actualizarEjercicio(
            ejercicio.getIdEjercicio(),
            new EjercicioRequest(
                ejercicio.getIdModulo(),
                "Identifica los tipos de datos (v2)",
                "cuestionario",
                "{\"pregunta\":\"¿Qué tipo guarda decimales?\",\"opciones\":[\"int\",\"double\"]}",
                "{\"respuesta\":\"double\"}"
            ),
            moduloJava().getIdDocente()
        );

        assertEquals("Identifica los tipos de datos (v2)", actualizado.titulo());
        assertEquals("{\"respuesta\":\"double\"}", actualizado.respuestaCorrecta());

        Ejercicio enBase = ejercicioRepository.findById(ejercicio.getIdEjercicio()).orElseThrow();
        assertEquals("{\"respuesta\":\"double\"}", enBase.getRespuestaCorrecta());
    }

    @Test
    void elDocenteNoPuedeEditarUnEjercicioAjeno() {
        Ejercicio ejercicio = ejercicioJava();

        assertThrows(ReglaNegocioException.class, () -> cursoService.actualizarEjercicio(
            ejercicio.getIdEjercicio(),
            new EjercicioRequest(ejercicio.getIdModulo(), "Intento ajeno", "abierta", "{}", "{}"),
            idAdministrador()
        ));
    }

    @Test
    void crearEditarYEliminarUnEjercicioPersisteEnLaBase() {
        Modulo modulo = moduloJava();

        EjercicioDto creado = cursoService.crearEjercicio(
            new EjercicioRequest(modulo.getIdModulo(), "Ejercicio de prueba", "abierta", "{\"texto\":\"x\"}", "{}"),
            modulo.getIdDocente()
        );

        EjercicioDto editado = cursoService.actualizarEjercicio(
            creado.idEjercicio(),
            new EjercicioRequest(
                modulo.getIdModulo(),
                "Ejercicio de prueba editado",
                "completar",
                "{\"texto\":\"___\"}",
                "{\"respuesta\":\"if\"}"
            ),
            modulo.getIdDocente()
        );

        assertEquals("Ejercicio de prueba editado", editado.titulo());
        assertEquals("completar", editado.tipo());
        assertEquals("{\"respuesta\":\"if\"}", editado.respuestaCorrecta());

        cursoService.eliminarEjercicio(editado.idEjercicio(), modulo.getIdDocente());

        assertFalse(ejercicioRepository.findById(editado.idEjercicio()).isPresent());
    }

    // ------------------------------------------------------------------
    // Indicadores
    // ------------------------------------------------------------------

    @Test
    void losKpisGlobalesEnlazanAlApartadoQueLosExplica() {
        for (KpiView kpi : panelService.kpisGlobales()) {
            assertTrue(kpi.getRuta().startsWith("/admin"), "Ruta inesperada: " + kpi.getRuta());
        }
    }

    @Test
    void losKpisDelDocenteEnlazanASusCursos() {
        for (KpiView kpi : panelService.kpisDocente(moduloJava().getIdDocente())) {
            assertEquals("/docente/cursos", kpi.getRuta());
        }
    }
}
