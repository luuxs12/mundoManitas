package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.InscripcionDao;
import com.mundomanitas.dao.PostulacionDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.dto.view.AlumnoProgresoView;
import com.mundomanitas.dto.view.CursoDocenteView;
import com.mundomanitas.dto.view.CursoGlobalView;
import com.mundomanitas.dto.view.KpiView;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Métricas de los paneles de docente y administrador.
 *
 * <p>Toda la lectura se hace dentro de una transacción porque las
 * asociaciones se cargan de forma perezosa y las vistas solo reciben
 * DTOs ya materializados.</p>
 */
@Service
public class PanelService {

    private final CursoDao cursoDao;
    private final UsuarioDao usuarioDao;
    private final InscripcionDao inscripcionDao;
    private final ProgresoDao progresoDao;
    private final PostulacionDao postulacionDao;

    public PanelService(
        CursoDao cursoDao,
        UsuarioDao usuarioDao,
        InscripcionDao inscripcionDao,
        ProgresoDao progresoDao,
        PostulacionDao postulacionDao
    ) {
        this.cursoDao = cursoDao;
        this.usuarioDao = usuarioDao;
        this.inscripcionDao = inscripcionDao;
        this.progresoDao = progresoDao;
        this.postulacionDao = postulacionDao;
    }

    // ------------------------------------------------------------------
    // Docente
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CursoDocenteView> cursosDocente(Integer idDocente) {
        return cursoDao.listarModulosPorDocente(idDocente).stream()
            .map(this::resumenCurso)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<KpiView> kpisDocente(Integer idDocente) {
        List<CursoDocenteView> cursos = cursosDocente(idDocente);

        long inscripciones = cursos.stream().mapToLong(CursoDocenteView::getInscritos).sum();
        long completados = cursos.stream().mapToLong(CursoDocenteView::getCompletados).sum();

        Set<Integer> alumnos = new LinkedHashSet<>();
        long enRiesgo = 0;

        for (CursoDocenteView curso : cursos) {
            for (Progreso progreso : progresoDao.listarPorModulo(curso.getIdModulo())) {
                alumnos.add(progreso.getIdEstudiante());

                if (porcentaje(progreso) < 30) {
                    enRiesgo++;
                }
            }
        }

        int promedio = cursos.isEmpty()
            ? 0
            : (int) Math.round(
                cursos.stream().mapToInt(CursoDocenteView::getPromedio).average().orElse(0)
            );

        List<KpiView> kpis = new ArrayList<>();
        kpis.add(new KpiView(
            "Cursos asignados",
            String.valueOf(cursos.size()),
            "Contenido bajo tu responsabilidad",
            "info",
            "curso"
        ));
        kpis.add(new KpiView(
            "Alumnos",
            String.valueOf(alumnos.size()),
            inscripciones + " inscripciones en total",
            "neutral",
            "alumnos"
        ));
        kpis.add(new KpiView(
            "Progreso promedio",
            promedio + "%",
            "Avance medio de tus cursos",
            promedio >= 60 ? "ok" : promedio >= 30 ? "warn" : "danger",
            "progreso"
        ));
        kpis.add(new KpiView(
            "Necesitan apoyo",
            String.valueOf(enRiesgo),
            "Alumnos por debajo del 30%",
            enRiesgo == 0 ? "ok" : "warn",
            "alerta"
        ));
        kpis.add(new KpiView(
            "Completados",
            String.valueOf(completados),
            "Alumnos que terminaron un curso",
            "ok",
            "check"
        ));

        return kpis;
    }

    @Transactional(readOnly = true)
    public List<AlumnoProgresoView> alumnosDeCurso(Integer idModulo) {
        Map<Integer, Usuario> estudiantes = usuarioDao.listarPorRol("ESTUDIANTE").stream()
            .collect(Collectors.toMap(Usuario::getIdUsuario, Function.identity()));

        Map<Integer, Progreso> progresos = progresoDao.listarPorModulo(idModulo).stream()
            .collect(Collectors.toMap(
                Progreso::getIdEstudiante,
                Function.identity(),
                (primero, segundo) -> primero
            ));

        List<AlumnoProgresoView> alumnos = new ArrayList<>();

        for (Inscripcion inscripcion : inscripcionDao.listarPorModulo(idModulo)) {
            Usuario estudiante = estudiantes.get(inscripcion.getIdEstudiante());

            if (estudiante == null) {
                continue;
            }

            Progreso progreso = progresos.get(estudiante.getIdUsuario());
            int porcentaje = porcentaje(progreso);

            alumnos.add(new AlumnoProgresoView(
                estudiante.getIdUsuario(),
                estudiante.getNombre(),
                estudiante.getEmail(),
                porcentaje,
                progresoDao.contarEjerciciosCorrectos(estudiante.getIdUsuario(), idModulo),
                progreso == null ? null : progreso.getFechaInicio(),
                progreso == null ? null : progreso.getFechaCompletado()
            ));
        }

        alumnos.sort(Comparator.comparingInt(AlumnoProgresoView::getPorcentaje).reversed());

        return alumnos;
    }

    @Transactional(readOnly = true)
    public Modulo cursoDeDocente(Integer idModulo, Integer idDocente) {
        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado"));

        if (modulo.getIdDocente() != null && !modulo.getIdDocente().equals(idDocente)) {
            throw new RecursoNoEncontradoException("Curso no encontrado");
        }

        return modulo;
    }

    @Transactional(readOnly = true)
    public CursoDocenteView resumenCursoDeDocente(Integer idModulo, Integer idDocente) {
        return resumenCurso(cursoDeDocente(idModulo, idDocente));
    }

    // ------------------------------------------------------------------
    // Administración
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<KpiView> kpisGlobales() {
        long usuarios = usuarioDao.contar();
        long estudiantes = usuarioDao.contarPorRol("ESTUDIANTE");
        long docentes = usuarioDao.contarPorRol("DOCENTE");
        long cursos = cursoDao.contarModulos();
        long inscripciones = inscripcionDao.contar();
        long pendientes = postulacionDao.contarPorEstado(EstadoPostulacion.PENDIENTE);

        Double promedio = progresoDao.promedioGlobal();
        int promedioGlobal = promedio == null ? 0 : (int) Math.round(promedio);

        List<Progreso> progresos = progresoDao.listarTodos();
        long enRiesgo = progresos.stream()
            .filter(progreso -> porcentaje(progreso) < 30)
            .count();

        List<KpiView> kpis = new ArrayList<>();
        kpis.add(new KpiView(
            "Usuarios",
            String.valueOf(usuarios),
            estudiantes + " estudiantes · " + docentes + " docentes",
            "info",
            "alumnos"
        ));
        kpis.add(new KpiView(
            "Cursos activos",
            String.valueOf(cursos),
            inscripciones + " inscripciones registradas",
            "neutral",
            "curso"
        ));
        kpis.add(new KpiView(
            "Progreso global",
            promedioGlobal + "%",
            "Promedio de toda la plataforma",
            promedioGlobal >= 60 ? "ok" : promedioGlobal >= 30 ? "warn" : "danger",
            "progreso"
        ));
        kpis.add(new KpiView(
            "Alumnos en riesgo",
            String.valueOf(enRiesgo),
            "Con avance menor al 30%",
            enRiesgo == 0 ? "ok" : "warn",
            "alerta"
        ));
        kpis.add(new KpiView(
            "Docentes",
            String.valueOf(docentes),
            "Responsables de contenido",
            "neutral",
            "docente"
        ));
        kpis.add(new KpiView(
            "Postulaciones",
            String.valueOf(pendientes),
            pendientes == 0
                ? "Sin solicitudes por revisar"
                : "Docentes pendientes de validación",
            pendientes == 0 ? "ok" : "warn",
            "postulacion"
        ));

        return kpis;
    }

    @Transactional(readOnly = true)
    public List<CursoGlobalView> cursosGlobales() {
        return cursoDao.listarModulos().stream()
            .map(modulo -> {
                long inscritos = inscripcionDao.contarPorModulo(modulo.getIdModulo());
                List<Progreso> progresos = progresoDao.listarPorModulo(modulo.getIdModulo());

                int promedio = progresos.isEmpty()
                    ? 0
                    : (int) Math.round(
                        progresos.stream().mapToInt(this::porcentaje).average().orElse(0)
                    );

                long completados = progresos.stream()
                    .filter(progreso -> porcentaje(progreso) >= 100)
                    .count();

                String docente = modulo.getIdDocente() == null
                    ? "Sin asignar"
                    : usuarioDao.buscarPorIdConRol(modulo.getIdDocente())
                        .map(Usuario::getNombre)
                        .orElse("Sin asignar");

                return new CursoGlobalView(
                    modulo.getIdModulo(),
                    modulo.getTitulo(),
                    modulo.getNivel(),
                    docente,
                    inscritos,
                    promedio,
                    completados
                );
            })
            .sorted(Comparator.comparingInt(CursoGlobalView::getPromedio).reversed())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Usuario> ultimosUsuarios(int limite) {
        return usuarioDao.listarConRol().stream()
            .sorted(Comparator.comparing(
                Usuario::getFechaRegistro,
                Comparator.nullsLast(Comparator.reverseOrder())
            ))
            .limit(Math.max(limite, 0))
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> usuariosPorRol() {
        Map<String, Long> resultado = new HashMap<>();
        resultado.put("ESTUDIANTE", usuarioDao.contarPorRol("ESTUDIANTE"));
        resultado.put("DOCENTE", usuarioDao.contarPorRol("DOCENTE"));
        resultado.put("ADMINISTRADOR", usuarioDao.contarPorRol("ADMINISTRADOR"));
        resultado.put("SISTEMAS", usuarioDao.contarPorRol("SISTEMAS"));
        resultado.put("PADRE", usuarioDao.contarPorRol("PADRE"));
        return resultado;
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private CursoDocenteView resumenCurso(Modulo modulo) {
        List<Progreso> progresos = progresoDao.listarPorModulo(modulo.getIdModulo());

        int promedio = progresos.isEmpty()
            ? 0
            : (int) Math.round(
                progresos.stream().mapToInt(this::porcentaje).average().orElse(0)
            );

        long completados = progresos.stream()
            .filter(progreso -> porcentaje(progreso) >= 100)
            .count();

        return new CursoDocenteView(
            modulo.getIdModulo(),
            modulo.getTitulo(),
            modulo.getNivel(),
            modulo.getOrden(),
            inscripcionDao.contarPorModulo(modulo.getIdModulo()),
            promedio,
            completados,
            cursoDao.listarVideos(modulo.getIdModulo()).size(),
            cursoDao.listarEjercicios(modulo.getIdModulo()).size()
        );
    }

    private int porcentaje(Progreso progreso) {
        return progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();
    }
}
