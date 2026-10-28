package com.mundomanitas.service;

import com.mundomanitas.dao.ComunidadDao;
import com.mundomanitas.dao.CompraDao;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.InscripcionDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.dto.view.AnuncioView;
import com.mundomanitas.dto.view.ResenaResumenView;
import com.mundomanitas.dto.view.ResenaView;
import com.mundomanitas.entity.Anuncio;
import com.mundomanitas.entity.Favorito;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Resena;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reglas de las funciones comunitarias del aula.
 *
 * <p>Los favoritos son privados del estudiante, las reseñas solo pueden
 * escribirlas quienes ya tienen acceso al curso y los anuncios solo los
 * publica el docente responsable del módulo.</p>
 */
@Service
public class ComunidadService {

    private static final int COMENTARIO_MINIMO = 3;
    private static final int COMENTARIO_MAXIMO = 500;
    private static final int TITULO_ANUNCIO_MINIMO = 4;
    private static final int TITULO_ANUNCIO_MAXIMO = 120;
    private static final int MENSAJE_ANUNCIO_MINIMO = 10;
    private static final int MENSAJE_ANUNCIO_MAXIMO = 1000;

    private final ComunidadDao comunidadDao;
    private final CursoDao cursoDao;
    private final InscripcionDao inscripcionDao;
    private final CompraDao compraDao;
    private final UsuarioDao usuarioDao;

    public ComunidadService(
        ComunidadDao comunidadDao,
        CursoDao cursoDao,
        InscripcionDao inscripcionDao,
        CompraDao compraDao,
        UsuarioDao usuarioDao
    ) {
        this.comunidadDao = comunidadDao;
        this.cursoDao = cursoDao;
        this.inscripcionDao = inscripcionDao;
        this.compraDao = compraDao;
        this.usuarioDao = usuarioDao;
    }

    // ------------------------------------------------------------------
    // Favoritos
    // ------------------------------------------------------------------

    @Transactional
    public boolean alternarFavorito(Integer idEstudiante, Integer idModulo) {
        if (idEstudiante == null) {
            throw new ReglaNegocioException("Debes iniciar sesión como estudiante.");
        }

        buscarModulo(idModulo);

        if (comunidadDao.existeFavorito(idEstudiante, idModulo)) {
            comunidadDao.eliminarFavorito(idEstudiante, idModulo);
            return false;
        }

        Favorito favorito = new Favorito();
        favorito.setIdEstudiante(idEstudiante);
        favorito.setIdModulo(idModulo);
        comunidadDao.guardarFavorito(favorito);

        return true;
    }

    @Transactional(readOnly = true)
    public Set<Integer> idsFavoritos(Integer idEstudiante) {
        if (idEstudiante == null) {
            return Set.of();
        }

        return comunidadDao.listarFavoritos(idEstudiante).stream()
            .map(Favorito::getIdModulo)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean esFavorito(Integer idEstudiante, Integer idModulo) {
        return idEstudiante != null
            && idModulo != null
            && comunidadDao.existeFavorito(idEstudiante, idModulo);
    }

    @Transactional(readOnly = true)
    public long contarFavoritos(Integer idEstudiante) {
        return idEstudiante == null ? 0 : comunidadDao.contarFavoritos(idEstudiante);
    }

    // ------------------------------------------------------------------
    // Reseñas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ResenaResumenView resenasDeCurso(
        Integer idModulo,
        Integer idEstudiante,
        boolean esEstudiante
    ) {
        buscarModulo(idModulo);

        List<Resena> resenas = comunidadDao.listarResenas(idModulo);
        Map<Integer, Usuario> estudiantes = usuarioDao.listarPorRol("ESTUDIANTE").stream()
            .collect(Collectors.toMap(Usuario::getIdUsuario, Function.identity()));

        List<ResenaView> vistas = resenas.stream()
            .map(resena -> toView(resena, estudiantes.get(resena.getIdEstudiante()), idEstudiante))
            .toList();

        ResenaView miResena = vistas.stream()
            .filter(ResenaView::isPropia)
            .findFirst()
            .orElse(null);

        boolean puedeResenar = esEstudiante
            && idEstudiante != null
            && tieneAcceso(idEstudiante, idModulo);

        double promedio = comunidadDao.promedioResenas(idModulo) == null
            ? 0
            : comunidadDao.promedioResenas(idModulo);

        return new ResenaResumenView(
            vistas,
            promedio,
            vistas.size(),
            puedeResenar,
            miResena
        );
    }

    @Transactional
    public void guardarResena(
        Integer idEstudiante,
        Integer idModulo,
        Integer calificacion,
        String comentario
    ) {
        if (idEstudiante == null) {
            throw new ReglaNegocioException("Debes iniciar sesión como estudiante.");
        }

        buscarModulo(idModulo);

        if (!tieneAcceso(idEstudiante, idModulo)) {
            throw new ReglaNegocioException(
                "Solo puedes reseñar un curso en el que ya estás inscrito."
            );
        }

        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new ReglaNegocioException("La calificación debe estar entre 1 y 5 estrellas.");
        }

        String texto = StringUtils.trimToEmpty(comentario);

        if (texto.length() < COMENTARIO_MINIMO) {
            throw new ReglaNegocioException(
                "Escribe un comentario de al menos " + COMENTARIO_MINIMO + " caracteres."
            );
        }

        if (texto.length() > COMENTARIO_MAXIMO) {
            throw new ReglaNegocioException(
                "El comentario no puede superar los " + COMENTARIO_MAXIMO + " caracteres."
            );
        }

        Resena resena = comunidadDao.buscarResena(idEstudiante, idModulo)
            .orElseGet(() -> {
                Resena nueva = new Resena();
                nueva.setIdEstudiante(idEstudiante);
                nueva.setIdModulo(idModulo);
                return nueva;
            });

        if (resena.getIdResena() != null) {
            resena.setFechaActualizacion(LocalDateTime.now());
        }

        resena.setCalificacion(calificacion);
        resena.setComentario(texto);

        comunidadDao.guardarResena(resena);
    }

    @Transactional
    public void eliminarResena(Integer idEstudiante, Integer idResena) {
        Resena resena = comunidadDao.buscarResenaPorId(idResena)
            .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada"));

        if (!resena.getIdEstudiante().equals(idEstudiante)) {
            throw new ReglaNegocioException("Solo puedes eliminar tu propia reseña.");
        }

        comunidadDao.eliminarResena(idResena);
    }

    @Transactional(readOnly = true)
    public long contarResenasDeDocente(Integer idDocente) {
        return comunidadDao.contarResenasDeDocente(idDocente);
    }

    @Transactional(readOnly = true)
    public long contarResenasDeEstudiante(Integer idEstudiante) {
        return idEstudiante == null ? 0 : comunidadDao.contarResenasDeEstudiante(idEstudiante);
    }

    @Transactional(readOnly = true)
    public double promedioResenasDeDocente(Integer idDocente) {
        Double promedio = comunidadDao.promedioResenasDeDocente(idDocente);
        return promedio == null ? 0 : promedio;
    }

    // ------------------------------------------------------------------
    // Anuncios
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AnuncioView> anunciosActivos(Integer idModulo) {
        return comunidadDao.listarAnunciosActivos(idModulo).stream()
            .map(this::toView)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AnuncioView> anunciosDelCurso(Integer idModulo) {
        return comunidadDao.listarAnuncios(idModulo).stream()
            .map(this::toView)
            .toList();
    }

    @Transactional
    public AnuncioView publicarAnuncio(
        Integer idModulo,
        Integer idAutor,
        boolean administrador,
        String titulo,
        String mensaje
    ) {
        Modulo modulo = buscarModulo(idModulo);

        if (!administrador && !idAutor.equals(modulo.getIdDocente())) {
            throw new ReglaNegocioException(
                "Solo el docente responsable del curso puede publicar anuncios."
            );
        }

        String tituloLimpio = StringUtils.trimToEmpty(titulo);
        String mensajeLimpio = StringUtils.trimToEmpty(mensaje);

        if (tituloLimpio.length() < TITULO_ANUNCIO_MINIMO
            || tituloLimpio.length() > TITULO_ANUNCIO_MAXIMO) {
            throw new ReglaNegocioException(
                "El título del anuncio debe tener entre " + TITULO_ANUNCIO_MINIMO
                    + " y " + TITULO_ANUNCIO_MAXIMO + " caracteres."
            );
        }

        if (mensajeLimpio.length() < MENSAJE_ANUNCIO_MINIMO
            || mensajeLimpio.length() > MENSAJE_ANUNCIO_MAXIMO) {
            throw new ReglaNegocioException(
                "El mensaje del anuncio debe tener entre " + MENSAJE_ANUNCIO_MINIMO
                    + " y " + MENSAJE_ANUNCIO_MAXIMO + " caracteres."
            );
        }

        Anuncio anuncio = new Anuncio();
        anuncio.setIdModulo(idModulo);
        anuncio.setIdDocente(idAutor);
        anuncio.setTitulo(tituloLimpio);
        anuncio.setMensaje(mensajeLimpio);
        anuncio.setActivo(true);

        return toView(comunidadDao.guardarAnuncio(anuncio));
    }

    @Transactional
    public void eliminarAnuncio(Integer idAnuncio, Integer idAutor, boolean administrador) {
        Anuncio anuncio = comunidadDao.buscarAnuncio(idAnuncio)
            .orElseThrow(() -> new RecursoNoEncontradoException("Anuncio no encontrado"));

        Modulo modulo = buscarModulo(anuncio.getIdModulo());

        if (!administrador && !idAutor.equals(modulo.getIdDocente())) {
            throw new ReglaNegocioException(
                "Solo el docente responsable del curso puede eliminar sus anuncios."
            );
        }

        comunidadDao.eliminarAnuncio(idAnuncio);
    }

    @Transactional(readOnly = true)
    public long contarAnunciosActivosDeDocente(Integer idDocente) {
        return comunidadDao.contarAnunciosActivosDeDocente(idDocente);
    }

    /**
     * Anuncios recientes de los cursos en los que el estudiante está inscrito.
     */
    @Transactional(readOnly = true)
    public List<AnuncioView> anunciosParaEstudiante(Integer idEstudiante, int limite) {
        if (idEstudiante == null) {
            return List.of();
        }

        List<AnuncioView> anuncios = new ArrayList<>();

        for (var inscripcion : inscripcionDao.listarPorEstudiante(idEstudiante)) {
            anuncios.addAll(anunciosActivos(inscripcion.getIdModulo()));
        }

        return anuncios.stream()
            .sorted(Comparator.comparing(AnuncioView::getFecha).reversed())
            .limit(Math.max(limite, 1))
            .toList();
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private boolean tieneAcceso(Integer idEstudiante, Integer idModulo) {
        Modulo modulo = buscarModulo(idModulo);

        if (modulo.isPremium()) {
            return compraDao.existe(idEstudiante, idModulo);
        }

        return inscripcionDao.existe(idEstudiante, idModulo);
    }

    private Modulo buscarModulo(Integer idModulo) {
        return cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));
    }

    private ResenaView toView(Resena resena, Usuario estudiante, Integer idActual) {
        String nombre = estudiante == null ? "Estudiante" : estudiante.getNombre();
        String inicial = nombre.isBlank()
            ? "E"
            : nombre.substring(0, 1).toUpperCase(Locale.ROOT);

        return new ResenaView(
            resena.getIdResena(),
            resena.getIdEstudiante(),
            nombre,
            inicial,
            resena.getCalificacion(),
            resena.getComentario(),
            resena.getFechaCreacion(),
            resena.getIdEstudiante().equals(idActual)
        );
    }

    private AnuncioView toView(Anuncio anuncio) {
        String autor = usuarioDao.buscarPorIdConRol(anuncio.getIdDocente())
            .map(Usuario::getNombre)
            .orElse("Docente");

        return new AnuncioView(
            anuncio.getIdAnuncio(),
            anuncio.getIdModulo(),
            anuncio.getTitulo(),
            anuncio.getMensaje(),
            autor,
            anuncio.getFechaPublicacion(),
            anuncio.isActivo()
        );
    }
}
