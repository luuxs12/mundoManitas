package com.mundomanitas.dao.impl;

import com.mundomanitas.dao.ComunidadDao;
import com.mundomanitas.entity.Anuncio;
import com.mundomanitas.entity.Favorito;
import com.mundomanitas.entity.Resena;
import com.mundomanitas.repository.AnuncioRepository;
import com.mundomanitas.repository.FavoritoRepository;
import com.mundomanitas.repository.ResenaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ComunidadDaoImpl implements ComunidadDao {

    private final FavoritoRepository favoritoRepository;
    private final ResenaRepository resenaRepository;
    private final AnuncioRepository anuncioRepository;

    public ComunidadDaoImpl(
        FavoritoRepository favoritoRepository,
        ResenaRepository resenaRepository,
        AnuncioRepository anuncioRepository
    ) {
        this.favoritoRepository = favoritoRepository;
        this.resenaRepository = resenaRepository;
        this.anuncioRepository = anuncioRepository;
    }

    @Override
    public boolean existeFavorito(Integer idEstudiante, Integer idModulo) {
        return favoritoRepository.existsByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public Optional<Favorito> buscarFavorito(Integer idEstudiante, Integer idModulo) {
        return favoritoRepository.findByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public Favorito guardarFavorito(Favorito favorito) {
        return favoritoRepository.save(favorito);
    }

    @Override
    public void eliminarFavorito(Integer idEstudiante, Integer idModulo) {
        favoritoRepository.deleteByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public List<Favorito> listarFavoritos(Integer idEstudiante) {
        return favoritoRepository.findByIdEstudianteOrderByFechaCreacionDesc(idEstudiante);
    }

    @Override
    public long contarFavoritos(Integer idEstudiante) {
        return favoritoRepository.countByIdEstudiante(idEstudiante);
    }

    @Override
    public void eliminarFavoritosPorModulo(Integer idModulo) {
        favoritoRepository.deleteByIdModulo(idModulo);
    }

    @Override
    public Optional<Resena> buscarResena(Integer idEstudiante, Integer idModulo) {
        return resenaRepository.findByIdEstudianteAndIdModulo(idEstudiante, idModulo);
    }

    @Override
    public Optional<Resena> buscarResenaPorId(Integer idResena) {
        return resenaRepository.findById(idResena);
    }

    @Override
    public Resena guardarResena(Resena resena) {
        return resenaRepository.save(resena);
    }

    @Override
    public List<Resena> listarResenas(Integer idModulo) {
        return resenaRepository.findByIdModuloOrderByFechaCreacionDesc(idModulo);
    }

    @Override
    public long contarResenas(Integer idModulo) {
        return resenaRepository.countByIdModulo(idModulo);
    }

    @Override
    public Double promedioResenas(Integer idModulo) {
        return resenaRepository.promedioPorModulo(idModulo);
    }

    @Override
    public long contarResenasDeDocente(Integer idDocente) {
        return resenaRepository.contarPorDocente(idDocente);
    }

    @Override
    public long contarResenasDeEstudiante(Integer idEstudiante) {
        return resenaRepository.countByIdEstudiante(idEstudiante);
    }

    @Override
    public Double promedioResenasDeDocente(Integer idDocente) {
        return resenaRepository.promedioPorDocente(idDocente);
    }

    @Override
    public void eliminarResena(Integer idResena) {
        resenaRepository.deleteById(idResena);
    }

    @Override
    public void eliminarResenasPorModulo(Integer idModulo) {
        resenaRepository.deleteByIdModulo(idModulo);
    }

    @Override
    public List<Anuncio> listarAnunciosActivos(Integer idModulo) {
        return anuncioRepository.findByIdModuloAndActivoTrueOrderByFechaPublicacionDesc(idModulo);
    }

    @Override
    public List<Anuncio> listarAnuncios(Integer idModulo) {
        return anuncioRepository.findByIdModuloOrderByFechaPublicacionDesc(idModulo);
    }

    @Override
    public Optional<Anuncio> buscarAnuncio(Integer idAnuncio) {
        return anuncioRepository.findById(idAnuncio);
    }

    @Override
    public Anuncio guardarAnuncio(Anuncio anuncio) {
        return anuncioRepository.save(anuncio);
    }

    @Override
    public void eliminarAnuncio(Integer idAnuncio) {
        anuncioRepository.deleteById(idAnuncio);
    }

    @Override
    public long contarAnunciosActivos(Integer idModulo) {
        return anuncioRepository.countByIdModuloAndActivoTrue(idModulo);
    }

    @Override
    public long contarAnunciosActivosDeDocente(Integer idDocente) {
        return anuncioRepository.contarActivosPorDocente(idDocente);
    }

    @Override
    public List<Anuncio> listarAnunciosRecientes() {
        return anuncioRepository.findTop6ByActivoTrueOrderByFechaPublicacionDesc();
    }

    @Override
    public void eliminarAnunciosPorModulo(Integer idModulo) {
        anuncioRepository.deleteByIdModulo(idModulo);
    }
}
