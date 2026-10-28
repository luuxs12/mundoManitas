package com.mundomanitas.service;

import com.mundomanitas.dto.api.ModuloDto;
import com.mundomanitas.dto.api.ModuloRequest;
import com.mundomanitas.dto.view.CompraView;
import com.mundomanitas.dto.view.CursoDetalleView;
import com.mundomanitas.entity.Compra;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.repository.CompraRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la compra simulada de cursos premium.
 *
 * <p>Verifican que la compra se registre en la base, que habilite el contenido
 * bloqueado y que las reglas de negocio impidan comprar cursos gratuitos o
 * duplicar un pago.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompraServiceTests {

    @Autowired
    private CompraService compraService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Integer idAlumno() {
        return usuarioRepository.findByEmailIgnoreCase("alumno@mundomanitas.com")
            .orElseThrow(() -> new IllegalStateException("El alumno demo no existe"))
            .getIdUsuario();
    }

    private ModuloDto crearPremium(String titulo, String precio, int orden) {
        return cursoService.crearModulo(
            new ModuloRequest(
                titulo,
                "Contenido de pago habilitado para las pruebas de compra.",
                "intermedio",
                orden,
                true,
                new BigDecimal(precio)
            ),
            null
        );
    }

    // ------------------------------------------------------------------
    // Registro de la compra
    // ------------------------------------------------------------------

    @Test
    void comprarUnCursoPremiumRegistraElPagoYHabilitaElContenido() {
        Integer alumno = idAlumno();
        ModuloDto modulo = crearPremium("Java Premium de prueba", "49.90", 90);

        CursoDetalleView antes = cursoService.detalle(modulo.idModulo(), alumno, true);
        assertTrue(antes.isBloqueado(), "El curso premium debe iniciar bloqueado");
        assertFalse(antes.isComprado());
        assertTrue(antes.getVideos().isEmpty(), "El contenido no debe exponerse antes de comprar");

        Compra compra = compraService.comprar(alumno, modulo.idModulo());

        assertNotNull(compra.getIdCompra());
        assertEquals(0, new BigDecimal("49.90").compareTo(compra.getMonto()));
        assertEquals("PAGADA", compra.getEstado());
        assertEquals("SIMULADO", compra.getMetodoPago());
        assertTrue(compra.getReferencia().startsWith("MM-" + alumno + "-" + modulo.idModulo()));
        assertNotNull(compra.getFechaCompra());

        assertTrue(compraService.estaComprada(alumno, modulo.idModulo()));
        assertTrue(inscripcionService.estaInscrito(alumno, modulo.idModulo()));

        CursoDetalleView despues = cursoService.detalle(modulo.idModulo(), alumno, true);
        assertTrue(despues.isComprado());
        assertFalse(despues.isBloqueado());
        assertTrue(despues.isInscrito());
    }

    @Test
    void comprarDosVecesElMismoCursoNoDuplicaElRegistro() {
        Integer alumno = idAlumno();
        ModuloDto modulo = crearPremium("Curso Premium idempotente", "35.00", 91);

        Compra primera = compraService.comprar(alumno, modulo.idModulo());
        Compra segunda = compraService.comprar(alumno, modulo.idModulo());

        assertEquals(primera.getIdCompra(), segunda.getIdCompra());
        assertEquals(1, compraRepository.countByIdModulo(modulo.idModulo()));
    }

    @Test
    void unCursoGratuitoNoSePuedeComprar() {
        Integer alumno = idAlumno();
        ModuloDto gratuito = cursoService.crearModulo(
            new ModuloRequest("Curso gratuito de prueba", "Sin costo", "basico", 92),
            null
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> compraService.comprar(alumno, gratuito.idModulo())
        );
        assertFalse(compraService.estaComprada(alumno, gratuito.idModulo()));
    }

    @Test
    void comprarUnModuloInexistenteLanzaRecursoNoEncontrado() {
        assertThrows(
            RecursoNoEncontradoException.class,
            () -> compraService.comprar(idAlumno(), 999_999)
        );
    }

    // ------------------------------------------------------------------
    // Reglas de acceso
    // ------------------------------------------------------------------

    @Test
    void noSePuedeInscribirEnUnCursoPremiumSinComprarlo() {
        Integer alumno = idAlumno();
        ModuloDto modulo = crearPremium("Curso Premium con candado", "25.00", 93);

        assertThrows(
            ReglaNegocioException.class,
            () -> cursoService.inscribir(modulo.idModulo(), alumno)
        );
        assertFalse(inscripcionService.estaInscrito(alumno, modulo.idModulo()));

        compraService.comprar(alumno, modulo.idModulo());

        cursoService.inscribir(modulo.idModulo(), alumno);
        assertTrue(inscripcionService.estaInscrito(alumno, modulo.idModulo()));
    }

    @Test
    void elTotalInvertidoSumaLasComprasDelEstudiante() {
        Integer alumno = idAlumno();
        BigDecimal inicial = compraService.totalInvertido(alumno);
        ModuloDto primero = crearPremium("Premium total uno", "10.50", 94);
        ModuloDto segundo = crearPremium("Premium total dos", "20.25", 95);

        compraService.comprar(alumno, primero.idModulo());
        compraService.comprar(alumno, segundo.idModulo());

        assertEquals(
            0,
            inicial.add(new BigDecimal("30.75")).compareTo(compraService.totalInvertido(alumno))
        );

        List<CompraView> compras = compraService.listarPorEstudiante(alumno);
        assertTrue(compras.stream().anyMatch(c -> "Premium total uno".equals(c.curso())));
        assertTrue(compras.stream().anyMatch(c -> "Premium total dos".equals(c.curso())));
    }
}
