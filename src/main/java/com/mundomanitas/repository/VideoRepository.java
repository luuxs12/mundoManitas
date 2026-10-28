package com.mundomanitas.repository;

import com.mundomanitas.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Integer> {

    List<Video> findByIdModuloOrderByOrdenAsc(Integer idModulo);

    Optional<Video> findFirstByIdModuloOrderByOrdenDesc(Integer idModulo);

    long countByIdModulo(Integer idModulo);

    boolean existsByIdModuloAndTituloIgnoreCase(Integer idModulo, String titulo);

    void deleteByIdModulo(Integer idModulo);
}
