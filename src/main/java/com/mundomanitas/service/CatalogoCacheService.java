package com.mundomanitas.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.entity.Ejercicio;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Video;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Caché en memoria con Google Guava para el catálogo educativo.
 * Se invalida al crear, actualizar o eliminar contenido.
 */
@Service
public class CatalogoCacheService {

    private final CursoDao cursoDao;
    private final Cache<String, Object> cache;

    public CatalogoCacheService(CursoDao cursoDao) {
        this.cursoDao = cursoDao;
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    public List<Modulo> modulos() {
        return obtener("modulos", cursoDao::listarModulos);
    }

    public List<Video> videos(Integer idModulo) {
        return obtener("videos:" + idModulo, () -> cursoDao.listarVideos(idModulo));
    }

    public List<Ejercicio> ejercicios(Integer idModulo) {
        return obtener("ejercicios:" + idModulo, () -> cursoDao.listarEjercicios(idModulo));
    }

    public void invalidarModulo(Integer idModulo) {
        cache.invalidate("videos:" + idModulo);
        cache.invalidate("ejercicios:" + idModulo);
        cache.invalidate("modulos");
    }

    public void invalidarTodo() {
        cache.invalidateAll();
    }

    public CacheStats estadisticas() {
        return cache.stats();
    }

    @SuppressWarnings("unchecked")
    private <T> T obtener(String clave, Callable<T> cargador) {
        try {
            return (T) cache.get(clave, cargador);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("No fue posible cargar el catálogo: " + clave, ex);
        }
    }
}
