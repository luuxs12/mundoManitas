package com.mundomanitas.repository;

import com.mundomanitas.entity.VideoProgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoProgresoRepository extends JpaRepository<VideoProgreso, Integer> {

    Optional<VideoProgreso> findByIdEstudianteAndIdVideo(Integer idEstudiante, Integer idVideo);

    List<VideoProgreso> findByIdEstudiante(Integer idEstudiante);

    @Query("""
        select vp.idVideo from VideoProgreso vp
        where vp.idEstudiante = :idEstudiante and vp.completado = true
        """)
    List<Integer> idsVideosCompletados(@Param("idEstudiante") Integer idEstudiante);

    @Query("""
        select count(vp) from VideoProgreso vp, Video v
        where vp.idVideo = v.idVideo
          and vp.idEstudiante = :idEstudiante
          and v.idModulo = :idModulo
          and vp.completado = true
        """)
    long contarCompletadosPorModulo(
        @Param("idEstudiante") Integer idEstudiante,
        @Param("idModulo") Integer idModulo
    );

    void deleteByIdVideo(Integer idVideo);
}
