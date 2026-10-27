package com.mundomanitas.service;

import com.mundomanitas.dto.view.AlumnoProgresoView;
import com.mundomanitas.dto.view.CursoDocenteView;
import com.mundomanitas.dto.view.CursoGlobalView;
import com.mundomanitas.dto.view.KpiView;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.repository.ModuloRepository;
import com.mundomanitas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los indicadores de los paneles: los valores salen de la base
 * de datos, las listas se ordenan y cada tarjeta ofrece una ruta real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PanelServiceTests {

    @Autowired
    private PanelService panelService;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Modulo moduloJava() {
        return moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(modulo -> "Fundamentos de Java".equals(modulo.getTitulo()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("El módulo demo no existe"));
    }

    private KpiView kpi(List<KpiView> kpis, String etiqueta) {
        return kpis.stream()
            .filter(kpi -> etiqueta.equals(kpi.getEtiqueta()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("KPI no encontrado: " + etiqueta));
    }

    @Test
    void elPanelGlobalResumeUsuariosCursosYPostulaciones() {
        List<KpiView> kpis = panelService.kpisGlobales();

        assertEquals(6, kpis.size());
        assertEquals(
            String.valueOf(usuarioRepository.count()),
            kpi(kpis, "Usuarios").getValor()
        );
        assertEquals(
            String.valueOf(moduloRepository.count()),
            kpi(kpis, "Cursos activos").getValor()
        );
        assertNotNull(kpi(kpis, "Progreso global").getValor());
        assertNotNull(kpi(kpis, "Alumnos en riesgo").getValor());
        assertNotNull(kpi(kpis, "Docentes").getValor());
        assertNotNull(kpi(kpis, "Postulaciones").getValor());
    }

    @Test
    void todosLosKpisGlobalesApuntanAUnApartadoDeAdministracion() {
        for (KpiView kpi : panelService.kpisGlobales()) {
            assertNotNull(kpi.getRuta());
            assertTrue(kpi.getRuta().startsWith("/admin"), "Ruta inesperada: " + kpi.getRuta());
            assertFalse(kpi.getRuta().isBlank());
        }
    }

    @Test
    void losCursosGlobalesSeOrdenanDeMayorAMenorPromedio() {
        List<CursoGlobalView> cursos = panelService.cursosGlobales();

        assertFalse(cursos.isEmpty());

        for (int indice = 1; indice < cursos.size(); indice++) {
            assertTrue(
                cursos.get(indice - 1).getPromedio() >= cursos.get(indice).getPromedio(),
                "Los cursos no están ordenados por promedio"
            );
        }

        CursoGlobalView primero = cursos.get(0);
        assertNotNull(primero.getTitulo());
        assertNotNull(primero.getDocente());
        assertTrue(primero.getInscritos() >= 0);
    }

    @Test
    void elPanelDocenteSoloCuentaLosCursosAsignados() {
        Integer idDocente = moduloJava().getIdDocente();

        List<CursoDocenteView> cursos = panelService.cursosDocente(idDocente);
        List<KpiView> kpis = panelService.kpisDocente(idDocente);

        assertEquals(5, kpis.size());
        assertEquals(String.valueOf(cursos.size()), kpi(kpis, "Cursos asignados").getValor());

        for (KpiView kpi : kpis) {
            assertEquals("/docente/cursos", kpi.getRuta());
        }
    }

    @Test
    void elDetalleDeAlumnosOrdenaElAvanceDeMayorAMenor() {
        List<AlumnoProgresoView> alumnos = panelService.alumnosDeCurso(moduloJava().getIdModulo());

        assertFalse(alumnos.isEmpty());

        for (AlumnoProgresoView alumno : alumnos) {
            assertNotNull(alumno.getNombre());
            assertNotNull(alumno.getEmail());
            assertTrue(alumno.getPorcentaje() >= 0 && alumno.getPorcentaje() <= 100);
        }

        for (int indice = 1; indice < alumnos.size(); indice++) {
            assertTrue(
                alumnos.get(indice - 1).getPorcentaje() >= alumnos.get(indice).getPorcentaje(),
                "Los alumnos no están ordenados por avance"
            );
        }
    }

    @Test
    void losUltimosUsuariosRespetanElLimiteSolicitado() {
        assertTrue(panelService.ultimosUsuarios(3).size() <= 3);
        assertTrue(panelService.ultimosUsuarios(0).isEmpty());
    }

    @Test
    void laDistribucionPorRolSumaElTotalDeUsuarios() {
        Map<String, Long> distribucion = panelService.usuariosPorRol();

        long total = distribucion.values().stream().mapToLong(Long::longValue).sum();

        assertEquals(usuarioRepository.count(), total);
        assertTrue(distribucion.containsKey("ESTUDIANTE"));
        assertTrue(distribucion.containsKey("DOCENTE"));
        assertTrue(distribucion.containsKey("ADMINISTRADOR"));
        assertTrue(distribucion.containsKey("SISTEMAS"));
        assertTrue(distribucion.containsKey("PADRE"));
        assertTrue(distribucion.size() >= 5);
    }

    @Test
    void unDocenteNoPuedeAbrirElCursoDeOtro() {
        Integer idAdministrador = usuarioRepository.findByEmailIgnoreCase("admin@mundomanitas.com")
            .orElseThrow()
            .getIdUsuario();

        assertThrows(RecursoNoEncontradoException.class, () ->
            panelService.cursoDeDocente(moduloJava().getIdModulo(), idAdministrador)
        );
    }
}
