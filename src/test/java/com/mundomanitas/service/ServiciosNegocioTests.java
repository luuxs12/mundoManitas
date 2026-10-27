package com.mundomanitas.service;

import com.mundomanitas.dto.api.IntentoResponse;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.exception.PasswordPolicyException;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.repository.EjercicioRepository;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.ProgresoRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de reglas de negocio: política de contraseñas, calificación de
 * ejercicios, progreso automático y documentos generados con Apache POI.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiciosNegocioTests {

    @Autowired
    private PasswordPolicy passwordPolicy;

    @Autowired
    private EjercicioService ejercicioService;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private CertificadoService certificadoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private ProgresoRepository progresoRepository;

    private Integer idAlumno() {
        return usuarioRepository.findByEmailIgnoreCase("alumno@mundomanitas.com")
            .orElseThrow(() -> new IllegalStateException("El alumno demo no existe"))
            .getIdUsuario();
    }

    private Ejercicio ejercicioPorTitulo(String titulo) {
        return ejercicioRepository.findAll().stream()
            .filter(ejercicio -> titulo.equals(ejercicio.getTitulo()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ejercicio no encontrado: " + titulo));
    }

    // ------------------------------------------------------------------
    // Política de contraseñas
    // ------------------------------------------------------------------

    @Test
    void laPoliticaAceptaUnaContrasenaFuerte() {
        assertDoesNotThrow(() -> passwordPolicy.validar("Manitas2026!"));
    }

    @Test
    void laPoliticaRechazaContrasenasDebiles() {
        assertThrows(PasswordPolicyException.class, () -> passwordPolicy.validar("123456"));
        assertThrows(PasswordPolicyException.class, () -> passwordPolicy.validar("sinmayusculas1!"));
        assertThrows(PasswordPolicyException.class, () -> passwordPolicy.validar("SinNumeros!"));
        assertThrows(PasswordPolicyException.class, () -> passwordPolicy.validar("SinEspeciales1"));
        assertThrows(PasswordPolicyException.class, () -> passwordPolicy.validar(null));
    }

    // ------------------------------------------------------------------
    // Ejercicios y progreso automático
    // ------------------------------------------------------------------

    @Test
    void unaRespuestaCorrectaCalificaElEjercicioYRecalculaElProgreso() {
        Ejercicio ejercicio = ejercicioPorTitulo("Identifica los tipos de datos");

        IntentoResponse respuesta = ejercicioService.responder(
            idAlumno(),
            ejercicio.getIdEjercicio(),
            "  INT  "
        );

        assertTrue(respuesta.correcto(), "La comparación debe ignorar mayúsculas y espacios");
        assertTrue(respuesta.porcentaje() >= 42);

        Progreso progreso = progresoRepository
            .findByIdEstudianteAndIdModulo(idAlumno(), ejercicio.getIdModulo())
            .orElseThrow();

        assertNotNull(progreso.getPorcentaje());
        assertTrue(progreso.getPorcentaje().intValue() >= 42);
    }

    @Test
    void unaRespuestaIncorrectaNoAcreditaElEjercicio() {
        Ejercicio ejercicio = ejercicioPorTitulo("Completa la estructura condicional");

        IntentoResponse respuesta = ejercicioService.responder(
            idAlumno(),
            ejercicio.getIdEjercicio(),
            "while"
        );

        assertFalse(respuesta.correcto());
        assertTrue(respuesta.mensaje().toLowerCase().contains("incorrecta"));
    }

    @Test
    void responderUnEjercicioInexistenteLanzaRecursoNoEncontrado() {
        assertThrows(
            com.mundomanitas.exception.RecursoNoEncontradoException.class,
            () -> ejercicioService.responder(idAlumno(), 99999, "int")
        );
    }

    // ------------------------------------------------------------------
    // Documentos con Apache POI
    // ------------------------------------------------------------------

    @Test
    void elReporteExcelSeGeneraConFormatoValido() {
        byte[] archivo = reporteService.generarReporteProgreso();

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);
        assertEquals('P', (char) archivo[0]);
        assertEquals('K', (char) archivo[1]);
    }

    @Test
    void elCertificadoWordSeGeneraAlCompletarElModulo() {
        Modulo modulo = moduloRepository.findAllByOrderByOrdenAsc().get(0);

        byte[] archivo = certificadoService.generar(idAlumno(), modulo.getIdModulo());

        assertNotNull(archivo);
        assertTrue(archivo.length > 0);
        assertEquals('P', (char) archivo[0]);
        assertEquals('K', (char) archivo[1]);
    }

    @Test
    void elCertificadoSeRechazaSiElModuloNoEstaCompletado() {
        Modulo modulo = moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(candidato -> candidato.getOrden() == 4)
            .findFirst()
            .orElseThrow();

        assertThrows(
            ReglaNegocioException.class,
            () -> certificadoService.generar(idAlumno(), modulo.getIdModulo())
        );
    }
}
