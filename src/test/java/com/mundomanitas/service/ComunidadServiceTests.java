package com.mundomanitas.service;

import com.mundomanitas.dto.view.AnuncioView;
import com.mundomanitas.dto.view.ResenaResumenView;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Resena;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.repository.AnuncioRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ResenaRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de la comunidad del aula: favoritos privados, reseñas solo con
 * acceso al curso y anuncios solo para el docente responsable.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ComunidadServiceTests {

    @Autowired
    private ComunidadService comunidadService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private AnuncioRepository anuncioRepository;

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private Modulo modulo(String titulo) {
        return moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(curso -> curso.getTitulo().equalsIgnoreCase(titulo))
            .findFirst()
            .orElseThrow();
    }

    @Test
    void alternarFavoritoAgregaYRetiraSinDuplicar() {
        Usuario alumno = usuario("alumno@mundomanitas.com");
        Modulo modulo = modulo("Pensamiento computacional");

        boolean inicial = comunidadService.esFavorito(alumno.getIdUsuario(), modulo.getIdModulo());
        assertFalse(inicial, "El curso de prueba no debe estar guardado al inicio");

        assertTrue(comunidadService.alternarFavorito(alumno.getIdUsuario(), modulo.getIdModulo()));
        assertTrue(comunidadService.esFavorito(alumno.getIdUsuario(), modulo.getIdModulo()));
        assertEquals(1, comunidadService.idsFavoritos(alumno.getIdUsuario()).stream()
            .filter(id -> id.equals(modulo.getIdModulo()))
            .count());

        assertFalse(comunidadService.alternarFavorito(alumno.getIdUsuario(), modulo.getIdModulo()));
        assertFalse(comunidadService.esFavorito(alumno.getIdUsuario(), modulo.getIdModulo()));
    }

    @Test
    void noSePuedeResenarUnCursoSinAcceso() {
        Usuario renzo = usuario("renzo@mundomanitas.com");
        Modulo java = modulo("Fundamentos de Java");

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.guardarResena(renzo.getIdUsuario(), java.getIdModulo(), 5, "Muy buen curso")
        );
    }

    @Test
    void elEstudianteInscritoPuedeCrearYActualizarSuResena() {
        Usuario renzo = usuario("renzo@mundomanitas.com");
        Modulo intro = modulo("Introducción a la programación");

        comunidadService.guardarResena(
            renzo.getIdUsuario(),
            intro.getIdModulo(),
            4,
            "Los ejemplos son claros y prácticos."
        );

        Resena creada = resenaRepository
            .findByIdEstudianteAndIdModulo(renzo.getIdUsuario(), intro.getIdModulo())
            .orElseThrow();

        assertEquals(4, creada.getCalificacion());

        comunidadService.guardarResena(
            renzo.getIdUsuario(),
            intro.getIdModulo(),
            5,
            "Actualizo mi opinión: el curso mejoró."
        );

        Resena actualizada = resenaRepository
            .findByIdEstudianteAndIdModulo(renzo.getIdUsuario(), intro.getIdModulo())
            .orElseThrow();

        assertEquals(5, actualizada.getCalificacion());
        assertEquals("Actualizo mi opinión: el curso mejoró.", actualizada.getComentario());
        assertTrue(actualizada.getFechaActualizacion() != null);

        ResenaResumenView resumen = comunidadService.resenasDeCurso(
            intro.getIdModulo(),
            renzo.getIdUsuario(),
            true
        );

        assertTrue(resumen.getTotal() >= 1);
        assertTrue(resumen.isPuedeResenar());
        assertTrue(resumen.getMiResena() != null);
    }

    @Test
    void laCalificacionYElComentarioSeValidan() {
        Usuario renzo = usuario("renzo@mundomanitas.com");
        Modulo intro = modulo("Introducción a la programación");

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.guardarResena(renzo.getIdUsuario(), intro.getIdModulo(), 6, "Comentario válido")
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.guardarResena(renzo.getIdUsuario(), intro.getIdModulo(), 5, "ok")
        );
    }

    @Test
    void soloElDocenteDuenoPublicaAnuncios() {
        Usuario docenteDemo = usuario("docente@mundomanitas.com");
        Usuario carlos = usuario("carlos@mundomanitas.com");
        Modulo java = modulo("Fundamentos de Java");

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.publicarAnuncio(
                java.getIdModulo(),
                carlos.getIdUsuario(),
                false,
                "Aviso ajeno",
                "Este docente no es el responsable del curso."
            )
        );

        AnuncioView anuncio = comunidadService.publicarAnuncio(
            java.getIdModulo(),
            docenteDemo.getIdUsuario(),
            false,
            "Repaso antes del examen",
            "El repaso será el viernes a las 6 p. m. en el aula virtual."
        );

        assertTrue(anuncio.getIdAnuncio() != null);
        assertTrue(comunidadService.anunciosDelCurso(java.getIdModulo()).stream()
            .anyMatch(a -> a.getTitulo().equals("Repaso antes del examen")));
    }

    @Test
    void elAdministradorPuedePublicarYLosTextosSeValidan() {
        Usuario admin = usuario("admin@mundomanitas.com");
        Modulo python = modulo("Python para principiantes");

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.publicarAnuncio(
                python.getIdModulo(),
                admin.getIdUsuario(),
                true,
                "Ok",
                "Este mensaje sí tiene la longitud mínima requerida."
            )
        );

        AnuncioView anuncio = comunidadService.publicarAnuncio(
            python.getIdModulo(),
            admin.getIdUsuario(),
            true,
            "Mantenimiento programado",
            "La plataforma estará en mantenimiento el domingo por la noche."
        );

        assertEquals("Mantenimiento programado", anuncio.getTitulo());
        assertTrue(anuncioRepository.existsByIdModuloAndTituloIgnoreCase(
            python.getIdModulo(),
            "Mantenimiento programado"
        ));
    }

    @Test
    void soloElAutorPuedeEliminarSuResena() {
        Usuario alumno = usuario("alumno@mundomanitas.com");
        Usuario maria = usuario("maria@mundomanitas.com");
        Modulo intro = modulo("Introducción a la programación");

        Resena deMaria = resenaRepository
            .findByIdEstudianteAndIdModulo(maria.getIdUsuario(), intro.getIdModulo())
            .orElseThrow();

        assertThrows(
            ReglaNegocioException.class,
            () -> comunidadService.eliminarResena(alumno.getIdUsuario(), deMaria.getIdResena())
        );

        comunidadService.eliminarResena(maria.getIdUsuario(), deMaria.getIdResena());

        assertFalse(resenaRepository.findById(deMaria.getIdResena()).isPresent());
    }

    @Test
    void elEstudianteRecibeLosAnunciosDeSusCursos() {
        Usuario alumno = usuario("alumno@mundomanitas.com");

        List<AnuncioView> anuncios = comunidadService.anunciosParaEstudiante(
            alumno.getIdUsuario(),
            3
        );

        assertFalse(anuncios.isEmpty(), "El alumno demo debe recibir anuncios de sus cursos");
        assertTrue(anuncios.size() <= 3);
    }
}
