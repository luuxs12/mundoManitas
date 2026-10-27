package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.InscripcionDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dto.api.EjercicioDto;
import com.mundomanitas.dto.api.EjercicioRequest;
import com.mundomanitas.dto.api.IntentoResponse;
import com.mundomanitas.dto.api.ModuloDto;
import com.mundomanitas.dto.api.ModuloRequest;
import com.mundomanitas.dto.api.VideoDto;
import com.mundomanitas.dto.api.VideoRequest;
import com.mundomanitas.dto.view.CursoDetalleView;
import com.mundomanitas.dto.view.CursoView;
import com.mundomanitas.dto.view.EjercicioView;
import com.mundomanitas.dto.view.ModuloView;
import com.mundomanitas.dto.view.VideoView;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Inscripcion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Video;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reglas de negocio del catálogo de cursos, inscripciones y contenido.
 */
@Service
public class CursoService {

    private static final int TAMANO_MAXIMO_PAGINA = 50;

    private final CursoDao cursoDao;
    private final InscripcionDao inscripcionDao;
    private final ProgresoDao progresoDao;
    private final InscripcionService inscripcionService;
    private final ProgresoService progresoService;
    private final EjercicioService ejercicioService;
    private final CatalogoCacheService cache;

    public CursoService(
        CursoDao cursoDao,
        InscripcionDao inscripcionDao,
        ProgresoDao progresoDao,
        InscripcionService inscripcionService,
        ProgresoService progresoService,
        EjercicioService ejercicioService,
        CatalogoCacheService cache
    ) {
        this.cursoDao = cursoDao;
        this.inscripcionDao = inscripcionDao;
        this.progresoDao = progresoDao;
        this.inscripcionService = inscripcionService;
        this.progresoService = progresoService;
        this.ejercicioService = ejercicioService;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public Page<CursoView> listar(
        String q,
        int page,
        int size,
        Integer idEstudiante,
        boolean esEstudiante
    ) {
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), TAMANO_MAXIMO_PAGINA),
            Sort.by(Sort.Direction.ASC, "orden")
        );

        Map<Integer, Progreso> progresos = esEstudiante
            ? progresoDao.listarPorEstudiante(idEstudiante)
                .stream()
                .collect(
                    Collectors.toMap(
                        Progreso::getIdModulo,
                        Function.identity(),
                        (primero, segundo) -> primero
                    )
                )
            : Map.of();

        Set<Integer> inscritos = esEstudiante
            ? inscripcionDao.listarPorEstudiante(idEstudiante)
                .stream()
                .map(Inscripcion::getIdModulo)
                .collect(Collectors.toSet())
            : Set.of();

        return cursoDao.buscarModulos(q, pageable)
            .map(modulo -> construirCursoView(
                modulo,
                progresos.get(modulo.getIdModulo()),
                esEstudiante,
                inscritos.contains(modulo.getIdModulo())
            ));
    }

    /**
     * Cursos en los que el estudiante ya está inscrito.
     */
    @Transactional(readOnly = true)
    public Page<CursoView> listarInscritos(
        Integer idEstudiante,
        String q,
        int page,
        int size
    ) {
        return listarFiltrado(idEstudiante, q, page, size, true);
    }

    /**
     * Cursos del catálogo en los que el estudiante aún no se ha inscrito.
     */
    @Transactional(readOnly = true)
    public Page<CursoView> listarDisponibles(
        Integer idEstudiante,
        String q,
        int page,
        int size
    ) {
        return listarFiltrado(idEstudiante, q, page, size, false);
    }

    private Page<CursoView> listarFiltrado(
        Integer idEstudiante,
        String q,
        int page,
        int size,
        boolean soloInscritos
    ) {
        Map<Integer, Progreso> progresos = progresoDao.listarPorEstudiante(idEstudiante)
            .stream()
            .collect(
                Collectors.toMap(
                    Progreso::getIdModulo,
                    Function.identity(),
                    (primero, segundo) -> primero
                )
            );

        Set<Integer> inscritos = inscripcionDao.listarPorEstudiante(idEstudiante)
            .stream()
            .map(Inscripcion::getIdModulo)
            .collect(Collectors.toSet());

        String filtro = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<Modulo> modulos = cursoDao.listarModulos().stream()
            .filter(modulo -> inscritos.contains(modulo.getIdModulo()) == soloInscritos)
            .filter(modulo -> coincide(modulo, filtro))
            .toList();

        int tamano = Math.min(Math.max(size, 1), TAMANO_MAXIMO_PAGINA);
        int pagina = Math.max(page, 0);
        int desde = Math.min(pagina * tamano, modulos.size());
        int hasta = Math.min(desde + tamano, modulos.size());

        List<CursoView> contenido = modulos.subList(desde, hasta).stream()
            .map(modulo -> construirCursoView(
                modulo,
                progresos.get(modulo.getIdModulo()),
                true,
                inscritos.contains(modulo.getIdModulo())
            ))
            .toList();

        return new PageImpl<>(
            contenido,
            PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.ASC, "orden")),
            modulos.size()
        );
    }

    @Transactional(readOnly = true)
    public long contarInscritos(Integer idEstudiante) {
        return inscripcionDao.contarPorEstudiante(idEstudiante);
    }

    @Transactional(readOnly = true)
    public long contarDisponibles(Integer idEstudiante) {
        return Math.max(
            cursoDao.contarModulos() - inscripcionDao.contarPorEstudiante(idEstudiante),
            0
        );
    }

    private boolean coincide(Modulo modulo, String filtro) {        if (filtro.isEmpty()) {
            return true;
        }

        return modulo.getTitulo() != null
            && modulo.getTitulo().toLowerCase(Locale.ROOT).contains(filtro)
            || modulo.getDescripcion() != null
            && modulo.getDescripcion().toLowerCase(Locale.ROOT).contains(filtro);
    }

    @Transactional(readOnly = true)
    public CursoDetalleView detalle(        Integer idModulo,
        Integer idEstudiante,
        boolean esEstudiante
    ) {
        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        List<Video> videos = cursoDao.listarVideos(idModulo);
        List<Ejercicio> ejercicios = cursoDao.listarEjercicios(idModulo);

        Progreso progreso = esEstudiante
            ? progresoDao.buscar(idEstudiante, idModulo).orElse(null)
            : null;

        int porcentaje = progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();

        Set<Integer> videosCompletados = esEstudiante
            ? new HashSet<>(progresoDao.idsVideosCompletados(idEstudiante))
            : Set.of();

        List<VideoView> videoViews = videos.stream()
            .map(video -> new VideoView(
                video.getIdVideo(),
                video.getTitulo(),
                video.getUrl(),
                video.getDuracion(),
                video.getDuracionMinutos(),
                videosCompletados.contains(video.getIdVideo())
            ))
            .toList();

        List<EjercicioView> ejercicioViews = ejercicios.stream()
            .map(ejercicio -> ejercicioService.toView(ejercicio, idEstudiante))
            .toList();

        int duracionTotal = videos.stream()
            .map(Video::getDuracion)
            .filter(duracion -> duracion != null)
            .mapToInt(Integer::intValue)
            .sum();

        int totalMinutos = (int) Math.ceil(duracionTotal / 60.0);

        VideoView siguienteVideo = videoViews.stream()
            .filter(video -> !video.isCompletado())
            .findFirst()
            .orElse(videoViews.isEmpty() ? null : videoViews.get(0));

        boolean inscrito = esEstudiante
            && inscripcionDao.existe(idEstudiante, idModulo);

        return new CursoDetalleView(
            new ModuloView(
                modulo.getIdModulo(),
                modulo.getTitulo(),
                modulo.getDescripcion(),
                modulo.getNivel(),
                modulo.getOrden()
            ),
            videoViews,
            ejercicioViews,
            porcentaje,
            estado(porcentaje),
            estadoClase(porcentaje),
            progreso == null ? null : progreso.getCalificacion(),
            esEstudiante,
            inscrito,
            videoViews.size() + ejercicioViews.size(),
            videoViews.size(),
            ejercicioViews.size(),
            totalMinutos,
            siguienteVideo
        );
    }

    @Transactional
    public void inscribir(Integer idModulo, Integer idEstudiante) {
        inscripcionService.inscribir(idEstudiante, idModulo);
    }

    @Transactional
    public void cancelarInscripcion(Integer idModulo, Integer idEstudiante) {
        inscripcionService.cancelar(idEstudiante, idModulo);
    }

    @Transactional
    public void marcarVideoCompletado(Integer idEstudiante, Integer idVideo) {
        progresoService.marcarVideoCompletado(idEstudiante, idVideo);
    }

    @Transactional
    public IntentoResponse responder(
        Integer idEstudiante,
        Integer idEjercicio,
        String respuesta
    ) {
        return ejercicioService.responder(idEstudiante, idEjercicio, respuesta);
    }

    // ------------------------------------------------------------------
    // Administración de contenido
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ModuloDto> listarModulosAdmin() {
        return cursoDao.listarModulos().stream().map(this::toDto).toList();
    }

    /**
     * Lista el contenido visible para el panel. Si se indica un docente,
     * solo devuelve sus cursos; {@code null} significa visión global.
     */
    @Transactional(readOnly = true)
    public List<ModuloDto> listarModulosAdmin(Integer idDocente) {
        List<Modulo> modulos = idDocente == null
            ? cursoDao.listarModulos()
            : cursoDao.listarModulosPorDocente(idDocente);

        return modulos.stream().map(this::toDto).toList();
    }

    /**
     * Comprueba que el docente sea dueño del curso antes de modificarlo.
     */
    @Transactional(readOnly = true)
    public void verificarPropiedad(Integer idModulo, Integer idDocente) {
        if (idDocente == null) {
            return;
        }

        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        if (!idDocente.equals(modulo.getIdDocente())) {
            throw new ReglaNegocioException(
                "Este curso no está asignado a tu cuenta."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<VideoDto> listarVideosAdmin(Integer idModulo) {
        return cursoDao.listarVideos(idModulo).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EjercicioDto> listarEjerciciosAdmin(Integer idModulo) {
        return cursoDao.listarEjercicios(idModulo).stream().map(this::toDto).toList();
    }

    @Transactional
    public ModuloDto crearModulo(ModuloRequest request) {
        return crearModulo(request, null);
    }

    @Transactional
    public ModuloDto crearModulo(ModuloRequest request, Integer idDocente) {
        if (cursoDao.existeTituloModulo(request.titulo())) {
            throw new ReglaNegocioException("Ya existe un módulo con ese título.");
        }

        Modulo modulo = new Modulo();
        modulo.setTitulo(request.titulo());
        modulo.setDescripcion(request.descripcion());
        modulo.setNivel(normalizarNivel(request.nivel()));
        modulo.setIdDocente(idDocente);
        modulo.setOrden(
            request.orden() == null ? cursoDao.siguienteOrdenModulo() : request.orden()
        );

        cache.invalidarTodo();

        return toDto(cursoDao.guardarModulo(modulo));
    }

    @Transactional
    public ModuloDto actualizarModulo(Integer idModulo, ModuloRequest request) {
        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        if (!modulo.getTitulo().equalsIgnoreCase(request.titulo())
            && cursoDao.existeTituloModulo(request.titulo())) {
            throw new ReglaNegocioException("Ya existe un módulo con ese título.");
        }

        modulo.setTitulo(request.titulo());
        modulo.setDescripcion(request.descripcion());
        modulo.setNivel(normalizarNivel(request.nivel()));

        if (request.orden() != null) {
            modulo.setOrden(request.orden());
        }

        cache.invalidarModulo(idModulo);

        return toDto(cursoDao.guardarModulo(modulo));
    }

    @Transactional
    public void eliminarModulo(Integer idModulo) {
        cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        cursoDao.listarVideos(idModulo).forEach(video ->
            progresoDao.eliminarVideoProgresoPorVideo(video.getIdVideo())
        );

        cursoDao.listarEjercicios(idModulo).forEach(ejercicio ->
            progresoDao.eliminarIntentosPorEjercicio(ejercicio.getIdEjercicio())
        );

        inscripcionDao.eliminarPorModulo(idModulo);
        progresoDao.eliminarProgresoPorModulo(idModulo);
        cursoDao.eliminarVideosDeModulo(idModulo);
        cursoDao.eliminarEjerciciosDeModulo(idModulo);
        cursoDao.eliminarModulo(idModulo);

        cache.invalidarTodo();
    }

    @Transactional
    public VideoDto crearVideo(VideoRequest request) {
        cursoDao.buscarModulo(request.idModulo())
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));
        Video video = new Video();
        video.setIdModulo(request.idModulo());
        video.setTitulo(request.titulo());
        video.setUrl(request.url());
        video.setDuracion(request.duracion());
        video.setOrden(
            request.orden() == null
                ? cursoDao.siguienteOrdenVideo(request.idModulo())
                : request.orden()
        );

        cache.invalidarModulo(request.idModulo());

        return toDto(cursoDao.guardarVideo(video));
    }

    @Transactional
    public VideoDto actualizarVideo(Integer idVideo, VideoRequest request) {
        Video video = cursoDao.buscarVideo(idVideo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Video no encontrado"));

        cursoDao.buscarModulo(request.idModulo())
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        Integer moduloAnterior = video.getIdModulo();

        video.setIdModulo(request.idModulo());
        video.setTitulo(request.titulo());
        video.setUrl(request.url());
        video.setDuracion(request.duracion());

        if (request.orden() != null) {
            video.setOrden(request.orden());
        }

        cache.invalidarModulo(moduloAnterior);
        cache.invalidarModulo(request.idModulo());

        return toDto(cursoDao.guardarVideo(video));
    }

    @Transactional
    public void eliminarVideo(Integer idVideo) {
        Video video = cursoDao.buscarVideo(idVideo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Video no encontrado"));

        progresoDao.eliminarVideoProgresoPorVideo(idVideo);
        cursoDao.eliminarVideo(idVideo);
        cache.invalidarModulo(video.getIdModulo());
    }

    @Transactional
    public EjercicioDto crearEjercicio(EjercicioRequest request) {
        cursoDao.buscarModulo(request.idModulo())
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setIdModulo(request.idModulo());
        ejercicio.setTitulo(request.titulo());
        ejercicio.setTipo(request.tipo());
        ejercicio.setContenido(request.contenido());
        ejercicio.setRespuestaCorrecta(request.respuestaCorrecta());

        cache.invalidarModulo(request.idModulo());

        return toDto(cursoDao.guardarEjercicio(ejercicio));
    }

    @Transactional
    public EjercicioDto actualizarEjercicio(Integer idEjercicio, EjercicioRequest request) {
        Ejercicio ejercicio = cursoDao.buscarEjercicio(idEjercicio)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));

        cursoDao.buscarModulo(request.idModulo())
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        Integer moduloAnterior = ejercicio.getIdModulo();

        ejercicio.setIdModulo(request.idModulo());
        ejercicio.setTitulo(request.titulo());
        ejercicio.setTipo(request.tipo());
        ejercicio.setContenido(request.contenido());
        ejercicio.setRespuestaCorrecta(request.respuestaCorrecta());

        cache.invalidarModulo(moduloAnterior);
        cache.invalidarModulo(request.idModulo());

        return toDto(cursoDao.guardarEjercicio(ejercicio));
    }

    @Transactional
    public void eliminarEjercicio(Integer idEjercicio) {
        Ejercicio ejercicio = cursoDao.buscarEjercicio(idEjercicio)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));

        progresoDao.eliminarIntentosPorEjercicio(idEjercicio);
        cursoDao.eliminarEjercicio(idEjercicio);
        cache.invalidarModulo(ejercicio.getIdModulo());
    }

    // ------------------------------------------------------------------
    // Variantes con control de propiedad (panel del docente)
    // ------------------------------------------------------------------

    @Transactional
    public VideoDto crearVideo(VideoRequest request, Integer idDocente) {
        verificarPropiedad(request.idModulo(), idDocente);
        return crearVideo(request);
    }

    @Transactional
    public void eliminarVideo(Integer idVideo, Integer idDocente) {
        Video video = cursoDao.buscarVideo(idVideo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Video no encontrado"));

        verificarPropiedad(video.getIdModulo(), idDocente);
        eliminarVideo(idVideo);
    }

    @Transactional
    public EjercicioDto crearEjercicio(EjercicioRequest request, Integer idDocente) {
        verificarPropiedad(request.idModulo(), idDocente);
        return crearEjercicio(request);
    }

    @Transactional
    public void eliminarEjercicio(Integer idEjercicio, Integer idDocente) {
        Ejercicio ejercicio = cursoDao.buscarEjercicio(idEjercicio)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ejercicio no encontrado"));

        verificarPropiedad(ejercicio.getIdModulo(), idDocente);
        eliminarEjercicio(idEjercicio);
    }

    // ------------------------------------------------------------------
    // Mapeos internos
    // ------------------------------------------------------------------

    private CursoView construirCursoView(
        Modulo modulo,
        Progreso progreso,
        boolean esEstudiante,
        boolean inscrito
    ) {
        int porcentaje = progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();

        return new CursoView(
            modulo.getIdModulo(),
            modulo.getTitulo(),
            modulo.getDescripcion(),
            modulo.getNivel(),
            modulo.getOrden(),
            porcentaje,
            estado(porcentaje),
            estadoClase(porcentaje),
            accion(porcentaje),
            coverClase(modulo.getNivel()),
            inicial(modulo.getTitulo()),
            esEstudiante && inscrito
        );
    }

    private String estado(int porcentaje) {
        if (porcentaje >= 100) {
            return "COMPLETADO";
        }
        if (porcentaje > 0) {
            return "EN PROGRESO";
        }
        return "POR INICIAR";
    }

    private String estadoClase(int porcentaje) {
        if (porcentaje >= 100) {
            return "completed";
        }
        if (porcentaje > 0) {
            return "progress";
        }
        return "new";
    }

    private String accion(int porcentaje) {
        if (porcentaje >= 100) {
            return "Revisar módulo";
        }
        if (porcentaje > 0) {
            return "Continuar módulo";
        }
        return "Comenzar módulo";
    }

    private String coverClase(String nivel) {
        if ("intermedio".equalsIgnoreCase(nivel)) {
            return "cover-intermedio";
        }
        if ("avanzado".equalsIgnoreCase(nivel)) {
            return "cover-avanzado";
        }
        return "cover-basico";
    }

    private String inicial(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return "M";
        }
        return titulo.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private String normalizarNivel(String nivel) {
        return nivel == null ? "basico" : nivel.trim().toLowerCase(Locale.ROOT);
    }

    private ModuloDto toDto(Modulo modulo) {
        return new ModuloDto(
            modulo.getIdModulo(),
            modulo.getTitulo(),
            modulo.getDescripcion(),
            modulo.getNivel(),
            modulo.getOrden()
        );
    }

    private VideoDto toDto(Video video) {
        return new VideoDto(
            video.getIdVideo(),
            video.getIdModulo(),
            video.getTitulo(),
            video.getUrl(),
            video.getDuracion(),
            video.getOrden()
        );
    }

    private EjercicioDto toDto(Ejercicio ejercicio) {
        return new EjercicioDto(
            ejercicio.getIdEjercicio(),
            ejercicio.getIdModulo(),
            ejercicio.getTitulo(),
            ejercicio.getTipo(),
            ejercicio.getContenido()
        );
    }
}
