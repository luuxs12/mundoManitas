package com.mundomanitas.service;

import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.PostulacionDao;
import com.mundomanitas.dto.api.UsuarioDto;
import com.mundomanitas.dto.api.UsuarioRequest;
import com.mundomanitas.dto.form.PostulacionDocenteForm;
import com.mundomanitas.dto.view.PostulacionView;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.PostulacionDocente;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reglas de negocio de la postulación docente.
 *
 * <p>El aspirante nunca crea su propia cuenta. Primero envía su evidencia
 * profesional; el administrador la revisa y, si la aprueba, este servicio
 * crea el usuario DOCENTE y le asigna un módulo concreto.</p>
 */
@Service
public class PostulacionService {

    private static final int OBSERVACION_MINIMA = 10;
    private static final int OBSERVACION_MAXIMA = 600;

    private final PostulacionDao postulacionDao;
    private final UsuarioService usuarioService;
    private final CursoDao cursoDao;
    private final PostulacionValidator validator;
    private final AuditoriaService auditoria;

    public PostulacionService(
        PostulacionDao postulacionDao,
        UsuarioService usuarioService,
        CursoDao cursoDao,
        PostulacionValidator validator,
        AuditoriaService auditoria
    ) {
        this.postulacionDao = postulacionDao;
        this.usuarioService = usuarioService;
        this.cursoDao = cursoDao;
        this.validator = validator;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------
    // Registro público
    // ------------------------------------------------------------------

    @Transactional
    public PostulacionDocente registrar(PostulacionDocenteForm form) {
        validator.validar(form);

        String email = normalizarEmail(form.getEmail());

        if (usuarioService.buscarPorEmail(email).isPresent()) {
            throw new ReglaNegocioException(
                "Ya existe una cuenta con ese correo. Inicia sesión o usa otro correo."
            );
        }

        if (postulacionDao.existePendientePorEmail(email)) {
            throw new ReglaNegocioException(
                "Ya tienes una postulación en revisión con ese correo. "
                    + "El administrador te contactará cuando la resuelva."
            );
        }

        PostulacionDocente postulacion = new PostulacionDocente();
        postulacion.setNombre(StringUtils.trim(form.getNombre()));
        postulacion.setEmail(email);
        postulacion.setTelefono(StringUtils.trim(form.getTelefono()));
        postulacion.setNivelLsp(
            StringUtils.trimToEmpty(form.getNivelLsp()).toUpperCase(Locale.ROOT)
        );
        postulacion.setCertificacionLsp(StringUtils.trim(form.getCertificacionLsp()));
        postulacion.setAniosExperiencia(form.getAniosExperiencia());
        postulacion.setAreaTi(StringUtils.trim(form.getAreaTi()));
        postulacion.setPortafolio(StringUtils.trim(form.getPortafolio()));
        postulacion.setMotivacion(StringUtils.trim(form.getMotivacion()));
        postulacion.setEstado(EstadoPostulacion.PENDIENTE);
        postulacion.setFechaPostulacion(LocalDateTime.now());

        PostulacionDocente guardada = postulacionDao.guardar(postulacion);

        auditoria.registrar(
            "POSTULACION_DOCENTE",
            email,
            "id=" + guardada.getIdPostulacion() + " estado=PENDIENTE"
        );

        return guardada;
    }

    // ------------------------------------------------------------------
    // Revisión del administrador
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PostulacionView> listar() {
        Map<Integer, String> titulos = titulosDeModulos();

        return postulacionDao.listarPorFechaDescendente().stream()
            .map(postulacion -> toView(postulacion, titulos))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PostulacionView> listarPendientes() {
        Map<Integer, String> titulos = titulosDeModulos();

        return postulacionDao.listarPorEstado(EstadoPostulacion.PENDIENTE).stream()
            .map(postulacion -> toView(postulacion, titulos))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PostulacionView> listarPorEstado(EstadoPostulacion estado) {
        Map<Integer, String> titulos = titulosDeModulos();

        return postulacionDao.listarPorEstado(estado).stream()
            .map(postulacion -> toView(postulacion, titulos))
            .toList();
    }

    @Transactional(readOnly = true)
    public PostulacionView obtener(Integer id) {
        return toView(buscar(id), titulosDeModulos());
    }

    @Transactional(readOnly = true)
    public long contarPendientes() {
        return postulacionDao.contarPorEstado(EstadoPostulacion.PENDIENTE);
    }

    /**
     * Aprueba la postulación: crea la cuenta DOCENTE con la contraseña
     * temporal definida por el administrador y asigna el módulo elegido.
     */
    @Transactional
    public UsuarioDto aprobar(
        Integer idPostulacion,
        Integer idModulo,
        String contrasena,
        String observacion
    ) {
        PostulacionDocente postulacion = buscar(idPostulacion);
        exigirPendiente(postulacion);

        if (idModulo == null) {
            throw new ReglaNegocioException(
                "Selecciona el módulo que quedará a cargo del nuevo docente."
            );
        }

        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "El módulo seleccionado no existe."
            ));

        UsuarioDto docente = usuarioService.crear(new UsuarioRequest(
            postulacion.getNombre(),
            postulacion.getEmail(),
            contrasena,
            "DOCENTE",
            true
        ));

        modulo.setIdDocente(docente.id());
        cursoDao.guardarModulo(modulo);

        postulacion.setEstado(EstadoPostulacion.APROBADA);
        postulacion.setFechaResolucion(LocalDateTime.now());
        postulacion.setObservacion(StringUtils.trimToNull(observacion));
        postulacion.setIdUsuario(docente.id());
        postulacion.setIdModulo(modulo.getIdModulo());

        postulacionDao.guardar(postulacion);

        auditoria.registrar(
            "POSTULACION_APROBADA",
            postulacion.getEmail(),
            "id=" + idPostulacion + " usuario=" + docente.id() + " modulo=" + idModulo
        );

        return docente;
    }

    @Transactional
    public void rechazar(Integer idPostulacion, String observacion) {
        PostulacionDocente postulacion = buscar(idPostulacion);
        exigirPendiente(postulacion);

        String motivo = StringUtils.trimToEmpty(observacion);

        if (motivo.length() < OBSERVACION_MINIMA) {
            throw new ReglaNegocioException(
                "Explica el motivo del rechazo (mínimo " + OBSERVACION_MINIMA + " caracteres)."
            );
        }

        if (motivo.length() > OBSERVACION_MAXIMA) {
            throw new ReglaNegocioException(
                "El motivo del rechazo no puede superar los " + OBSERVACION_MAXIMA + " caracteres."
            );
        }

        postulacion.setEstado(EstadoPostulacion.RECHAZADA);
        postulacion.setFechaResolucion(LocalDateTime.now());
        postulacion.setObservacion(motivo);

        postulacionDao.guardar(postulacion);

        auditoria.registrar(
            "POSTULACION_RECHAZADA",
            postulacion.getEmail(),
            "id=" + idPostulacion
        );
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private PostulacionDocente buscar(Integer idPostulacion) {
        if (idPostulacion == null) {
            throw new RecursoNoEncontradoException("La postulación no existe.");
        }

        return postulacionDao.buscarPorId(idPostulacion)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "La postulación no existe."
            ));
    }

    private void exigirPendiente(PostulacionDocente postulacion) {
        if (!postulacion.isPendiente()) {
            throw new ReglaNegocioException(
                "Esta postulación ya fue resuelta y no puede modificarse."
            );
        }
    }

    private Map<Integer, String> titulosDeModulos() {
        Map<Integer, String> titulos = new HashMap<>();

        for (Modulo modulo : cursoDao.listarModulos()) {
            titulos.put(modulo.getIdModulo(), modulo.getTitulo());
        }

        return titulos;
    }

    private PostulacionView toView(
        PostulacionDocente postulacion,
        Map<Integer, String> titulos
    ) {
        String modulo = postulacion.getIdModulo() == null
            ? null
            : titulos.getOrDefault(postulacion.getIdModulo(), "Módulo no disponible");

        return new PostulacionView(
            postulacion.getIdPostulacion(),
            postulacion.getNombre(),
            postulacion.getEmail(),
            postulacion.getTelefono(),
            postulacion.getNivelLsp(),
            postulacion.getCertificacionLsp(),
            postulacion.getAniosExperiencia(),
            postulacion.getAreaTi(),
            postulacion.getPortafolio(),
            postulacion.getMotivacion(),
            postulacion.getEstado(),
            postulacion.getObservacion(),
            postulacion.getFechaPostulacion(),
            postulacion.getFechaResolucion(),
            postulacion.getIdUsuario(),
            modulo
        );
    }

    private String normalizarEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
    }
}
