package com.mundomanitas.config;

import com.mundomanitas.repository.AnuncioRepository;
import com.mundomanitas.repository.CompraRepository;
import com.mundomanitas.repository.EjercicioRepository;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que las semillas creen los datos mínimos sobre una base vacía
 * (H2 arranca sin tablas ni registros) y que ejecutarlas de nuevo no
 * duplique información.
 */
@SpringBootTest
@ActiveProfiles("test")
class DatosInicialesTests {

    @Autowired
    @Qualifier("crearDatosIniciales")
    private CommandLineRunner semillas;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ModuloSistemaRepository moduloSistemaRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private PostulacionDocenteRepository postulacionRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private AnuncioRepository anuncioRepository;

    @Test
    void laBaseVaciaQuedaSembradaConUsuariosRolesYContenido() {
        assertEquals(16, usuarioRepository.count(), "Deben existir las dieciséis cuentas demo");
        assertEquals(5, rolRepository.count(), "Deben existir los cinco roles del sistema");
        assertEquals(9, moduloSistemaRepository.count(), "Deben existir los nueve módulos de navegación");

        assertTrue(moduloRepository.count() >= 12, "Deben existir al menos doce cursos demo");
        assertTrue(videoRepository.count() >= 24, "Cada curso demo debe tener videos");
        assertTrue(ejercicioRepository.count() >= 12, "Cada curso demo debe tener ejercicios");
        assertTrue(progresoRepository.count() >= 20);
        assertTrue(inscripcionRepository.count() >= 20);

        assertTrue(usuarioRepository.findByEmailIgnoreCase("alumno@mundomanitas.com").isPresent());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("maria@mundomanitas.com").isPresent());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("docente@mundomanitas.com").isPresent());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("admin@mundomanitas.com").isPresent());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("sistemas@mundomanitas.com").isPresent());
    }

    @Test
    void losCursosDemoTienenUnDocenteAsignado() {
        Set<Integer> docentes = Set.of(
            usuarioRepository.findByEmailIgnoreCase("docente@mundomanitas.com").orElseThrow().getIdUsuario(),
            usuarioRepository.findByEmailIgnoreCase("carlos@mundomanitas.com").orElseThrow().getIdUsuario(),
            usuarioRepository.findByEmailIgnoreCase("elena@mundomanitas.com").orElseThrow().getIdUsuario(),
            usuarioRepository.findByEmailIgnoreCase("jorge@mundomanitas.com").orElseThrow().getIdUsuario()
        );

        assertTrue(moduloRepository.count() > 0);
        assertTrue(
            moduloRepository.findAllByOrderByOrdenAsc().stream()
                .allMatch(modulo -> docentes.contains(modulo.getIdDocente())),
            "Todos los cursos demo deben pertenecer a uno de los docentes de ejemplo"
        );
    }

    @Test
    void laComunidadDemoTieneFavoritosResenasYAnuncios() {
        assertTrue(favoritoRepository.count() >= 7, "Deben existir favoritos de ejemplo");
        assertTrue(resenaRepository.count() >= 6, "Deben existir reseñas de ejemplo");
        assertTrue(anuncioRepository.count() >= 6, "Deben existir anuncios de ejemplo");
        assertTrue(compraRepository.count() >= 5, "Deben existir compras premium simuladas");
    }

    @Test
    void ejecutarLasSemillasDeNuevoNoDuplicaRegistros() throws Exception {
        long usuarios = usuarioRepository.count();
        long roles = rolRepository.count();
        long modulosSistema = moduloSistemaRepository.count();
        long modulos = moduloRepository.count();
        long videos = videoRepository.count();
        long ejercicios = ejercicioRepository.count();
        long progresos = progresoRepository.count();
        long inscripciones = inscripcionRepository.count();
        long postulaciones = postulacionRepository.count();
        long compras = compraRepository.count();
        long favoritos = favoritoRepository.count();
        long resenas = resenaRepository.count();
        long anuncios = anuncioRepository.count();

        semillas.run();

        assertEquals(usuarios, usuarioRepository.count());
        assertEquals(roles, rolRepository.count());
        assertEquals(modulosSistema, moduloSistemaRepository.count());
        assertEquals(modulos, moduloRepository.count());
        assertEquals(videos, videoRepository.count());
        assertEquals(ejercicios, ejercicioRepository.count());
        assertEquals(progresos, progresoRepository.count());
        assertEquals(inscripciones, inscripcionRepository.count());
        assertEquals(postulaciones, postulacionRepository.count());
        assertEquals(compras, compraRepository.count());
        assertEquals(favoritos, favoritoRepository.count());
        assertEquals(resenas, resenaRepository.count());
        assertEquals(anuncios, anuncioRepository.count());
    }

    @Test
    void losRolesTienenAsignadosSusModulosDeNavegacion() {
        var estudiante = rolRepository.findByCodigoIgnoreCase("ESTUDIANTE").orElseThrow();
        var docente = rolRepository.findByCodigoIgnoreCase("DOCENTE").orElseThrow();
        var administrador = rolRepository.findByCodigoIgnoreCase("ADMINISTRADOR").orElseThrow();

        assertNotNull(estudiante.getModulos());
        assertTrue(estudiante.getModulos().size() >= 3);
        assertTrue(estudiante.getModulos().stream().anyMatch(m -> "FAVORITOS".equals(m.getCodigo())));
        assertTrue(docente.getModulos().stream().anyMatch(m -> "ADMIN_CONTENIDO".equals(m.getCodigo())));
        assertTrue(docente.getModulos().stream().anyMatch(m -> "DOCENTE_CURSOS".equals(m.getCodigo())));
        assertTrue(administrador.getModulos().stream().anyMatch(m -> "ADMIN_USUARIOS".equals(m.getCodigo())));
        assertTrue(administrador.getModulos().stream().anyMatch(m -> "ADMIN_PANEL".equals(m.getCodigo())));
        assertTrue(administrador.getModulos().stream().anyMatch(m -> "ADMIN_POSTULACIONES".equals(m.getCodigo())));
    }

    @Test
    void laPostulacionDocenteDemoQuedaPendienteDeRevision() {
        assertEquals(1, postulacionRepository.count());
        assertTrue(postulacionRepository.findAll().stream()
            .allMatch(postulacion -> postulacion.getEstado().estaPendiente()));
    }
}
