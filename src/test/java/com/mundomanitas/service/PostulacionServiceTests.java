package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.PostulacionDao;
import com.mundomanitas.dto.form.PostulacionDocenteForm;
import com.mundomanitas.dto.view.PostulacionView;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.PostulacionDocente;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de la postulación docente: el aspirante no crea su cuenta, el
 * administrador valida y solo entonces se habilita el acceso DOCENTE.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostulacionServiceTests {

    @Autowired
    private PostulacionService postulacionService;

    @Autowired
    private PostulacionDao postulacionDao;

    @Autowired
    private PostulacionValidator validator;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoDao cursoDao;

    @Test
    void unaPostulacionValidaQuedaPendienteYNoCreaCuenta() {
        PostulacionDocenteForm form = formularioValido("carlos.mendoza@ejemplo.com");
        long pendientesAntes = postulacionService.contarPendientes();

        PostulacionDocente guardada = postulacionService.registrar(form);

        assertNotNull(guardada.getIdPostulacion());
        assertEquals(EstadoPostulacion.PENDIENTE, guardada.getEstado());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("carlos.mendoza@ejemplo.com").isEmpty());
        assertEquals(pendientesAntes + 1, postulacionService.contarPendientes());
    }

    @Test
    void laValidacionExigeLspSuficienteYExperienciaEnTi() {
        PostulacionDocenteForm form = formularioValido("sin.requisitos@ejemplo.com");
        form.setNivelLsp("A2");
        form.setAniosExperiencia(0);
        form.setMotivacion("muy corta");

        ReglaNegocioException excepcion = assertThrows(
            ReglaNegocioException.class,
            () -> validator.validar(form)
        );

        assertTrue(excepcion.getMessage().contains("B1, B2, C1 o C2"));
        assertTrue(excepcion.getMessage().contains("al menos 1 año de experiencia en TI"));
        assertTrue(excepcion.getMessage().contains("mínimo 80 caracteres"));
    }

    @Test
    void noSePuedePostularDosVecesConElMismoCorreoPendiente() {
        PostulacionDocenteForm form = formularioValido("duplicado@ejemplo.com");

        postulacionService.registrar(form);

        assertThrows(
            ReglaNegocioException.class,
            () -> postulacionService.registrar(form)
        );
    }

    @Test
    void aprobarCreaLaCuentaDocenteYAsignaElModulo() {
        PostulacionDocente postulacion = postulacionDemo();
        List<Modulo> modulos = cursoDao.listarModulos();
        Modulo modulo = modulos.get(0);

        postulacionService.aprobar(
            postulacion.getIdPostulacion(),
            modulo.getIdModulo(),
            "Clave#2026",
            "Portafolio verificado"
        );

        Usuario docente = usuarioRepository.findByEmailIgnoreCase("ana.quispe@ejemplo.com")
            .orElseThrow();

        assertEquals("DOCENTE", docente.getRol().getCodigo());
        assertTrue(docente.isActivo());

        Modulo asignado = cursoDao.buscarModulo(modulo.getIdModulo()).orElseThrow();
        assertEquals(docente.getIdUsuario(), asignado.getIdDocente());

        PostulacionDocente resuelta = postulacionDao
            .buscarPorId(postulacion.getIdPostulacion())
            .orElseThrow();

        assertEquals(EstadoPostulacion.APROBADA, resuelta.getEstado());
        assertEquals(docente.getIdUsuario(), resuelta.getIdUsuario());
        assertEquals(modulo.getIdModulo(), resuelta.getIdModulo());
        assertNotNull(resuelta.getFechaResolucion());
    }

    @Test
    void rechazarExigeUnMotivoYNoCreaCuenta() {
        PostulacionDocente postulacion = postulacionDemo();

        assertThrows(
            ReglaNegocioException.class,
            () -> postulacionService.rechazar(postulacion.getIdPostulacion(), "corto")
        );

        postulacionService.rechazar(
            postulacion.getIdPostulacion(),
            "La certificación de LSP no pudo ser verificada."
        );

        PostulacionDocente resuelta = postulacionDao
            .buscarPorId(postulacion.getIdPostulacion())
            .orElseThrow();

        assertEquals(EstadoPostulacion.RECHAZADA, resuelta.getEstado());
        assertTrue(usuarioRepository.findByEmailIgnoreCase("ana.quispe@ejemplo.com").isEmpty());
    }

    @Test
    void elListadoExponeElEstadoYElModuloAsignado() {
        List<PostulacionView> postulaciones = postulacionService.listar();

        assertTrue(postulaciones.stream().anyMatch(postulacion ->
            "ana.quispe@ejemplo.com".equals(postulacion.getEmail())
                && postulacion.isPendiente()
        ));
    }

    private PostulacionDocente postulacionDemo() {
        return postulacionDao.buscarUltimaPorEmail("ana.quispe@ejemplo.com")
            .orElseThrow();
    }

    private PostulacionDocenteForm formularioValido(String email) {
        PostulacionDocenteForm form = new PostulacionDocenteForm();
        form.setNombre("Carlos Alberto Mendoza");
        form.setEmail(email);
        form.setTelefono("987654321");
        form.setNivelLsp("C1");
        form.setCertificacionLsp("Certificación de LSP nivel C1 - INSOR Perú");
        form.setAniosExperiencia(5);
        form.setAreaTi("Desarrollo web accesible");
        form.setPortafolio("https://portafolio.ejemplo.com/carlos");
        form.setMotivacion(
            "Quiero compartir mi experiencia en desarrollo web con la comunidad sorda "
                + "y construir materiales visuales que no dependan del audio."
        );
        form.setAceptaTerminos(true);
        return form;
    }
}
