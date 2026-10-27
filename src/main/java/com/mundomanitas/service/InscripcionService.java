package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.InscripcionDao;
import com.mundomanitas.dto.api.InscripcionDto;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gestión de inscripciones de estudiantes a módulos.
 */
@Service
public class InscripcionService {

    private final InscripcionDao inscripcionDao;
    private final CursoDao cursoDao;
    private final ProgresoService progresoService;

    public InscripcionService(
        InscripcionDao inscripcionDao,
        CursoDao cursoDao,
        ProgresoService progresoService
    ) {
        this.inscripcionDao = inscripcionDao;
        this.cursoDao = cursoDao;
        this.progresoService = progresoService;
    }

    @Transactional
    public Inscripcion inscribir(Integer idEstudiante, Integer idModulo) {
        cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        Inscripcion existente = inscripcionDao
            .buscar(idEstudiante, idModulo)
            .orElse(null);

        if (existente != null) {
            return existente;
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setIdEstudiante(idEstudiante);
        inscripcion.setIdModulo(idModulo);
        inscripcion.setEstado("ACTIVA");

        Inscripcion guardada = inscripcionDao.guardar(inscripcion);

        // Al inscribirse se crea el registro de progreso en 0 %.
        progresoService.obtenerOCrear(idEstudiante, idModulo);

        return guardada;
    }

    @Transactional
    public void cancelar(Integer idEstudiante, Integer idModulo) {
        inscripcionDao.eliminar(idEstudiante, idModulo);
    }

    @Transactional(readOnly = true)
    public boolean estaInscrito(Integer idEstudiante, Integer idModulo) {
        return inscripcionDao.existe(idEstudiante, idModulo);
    }

    @Transactional(readOnly = true)
    public long contar(Integer idEstudiante) {
        return inscripcionDao.contarPorEstudiante(idEstudiante);
    }

    @Transactional(readOnly = true)
    public List<InscripcionDto> listarPorEstudiante(Integer idEstudiante) {
        Map<Integer, Modulo> modulos = cursoDao.listarModulos()
            .stream()
            .collect(Collectors.toMap(Modulo::getIdModulo, Function.identity()));

        return inscripcionDao.listarPorEstudiante(idEstudiante)
            .stream()
            .map(inscripcion -> {
                Modulo modulo = modulos.get(inscripcion.getIdModulo());

                return new InscripcionDto(
                    inscripcion.getIdInscripcion(),
                    inscripcion.getIdModulo(),
                    modulo == null ? "Módulo" : modulo.getTitulo(),
                    inscripcion.getFechaInscripcion(),
                    inscripcion.getEstado()
                );
            })
            .toList();
    }
}
