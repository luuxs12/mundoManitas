package com.mundomanitas.dao;

import com.mundomanitas.entity.Anuncio;
import com.mundomanitas.entity.Favorito;
import com.mundomanitas.entity.Resena;

import java.util.List;
import java.util.Optional;

/**
 * Contrato DAO de las funciones comunitarias del aula: favoritos, reseñas
 * y anuncios. Se agrupan en un solo contrato porque comparten el mismo
 * ciclo de vida dentro de un curso.
 */
public interface ComunidadDao {

    // ------------------------------------------------------------------
    // Favoritos
    // ------------------------------------------------------------------

    boolean existeFavorito(Integer idEstudiante, Integer idModulo);

    Optional<Favorito> buscarFavorito(Integer idEstudiante, Integer idModulo);

    Favorito guardarFavorito(Favorito favorito);

    void eliminarFavorito(Integer idEstudiante, Integer idModulo);

    List<Favorito> listarFavoritos(Integer idEstudiante);

    long contarFavoritos(Integer idEstudiante);

    void eliminarFavoritosPorModulo(Integer idModulo);

    // ------------------------------------------------------------------
    // Reseñas
    // ------------------------------------------------------------------

    Optional<Resena> buscarResena(Integer idEstudiante, Integer idModulo);

    Optional<Resena> buscarResenaPorId(Integer idResena);

    Resena guardarResena(Resena resena);

    List<Resena> listarResenas(Integer idModulo);

    long contarResenas(Integer idModulo);

    Double promedioResenas(Integer idModulo);

    long contarResenasDeDocente(Integer idDocente);

    long contarResenasDeEstudiante(Integer idEstudiante);

    Double promedioResenasDeDocente(Integer idDocente);

    void eliminarResena(Integer idResena);

    void eliminarResenasPorModulo(Integer idModulo);

    // ------------------------------------------------------------------
    // Anuncios
    // ------------------------------------------------------------------

    List<Anuncio> listarAnunciosActivos(Integer idModulo);

    List<Anuncio> listarAnuncios(Integer idModulo);

    Optional<Anuncio> buscarAnuncio(Integer idAnuncio);

    Anuncio guardarAnuncio(Anuncio anuncio);

    void eliminarAnuncio(Integer idAnuncio);

    long contarAnunciosActivos(Integer idModulo);

    long contarAnunciosActivosDeDocente(Integer idDocente);

    List<Anuncio> listarAnunciosRecientes();

    void eliminarAnunciosPorModulo(Integer idModulo);
}
