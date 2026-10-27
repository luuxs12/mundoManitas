package com.mundomanitas.repository;

import com.mundomanitas.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = {"rol", "rol.modulos"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"rol"})
    @Query("select u from Usuario u where u.idUsuario = :id")
    Optional<Usuario> buscarConRolPorId(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"rol"})
    @Query("select u from Usuario u order by u.nombre asc")
    List<Usuario> listarConRol();

    @EntityGraph(attributePaths = {"rol"})
    @Query("select u from Usuario u")
    Page<Usuario> listarConRol(Pageable pageable);

    @EntityGraph(attributePaths = {"rol"})
    @Query("""
        select u from Usuario u
        where lower(u.nombre) like lower(concat('%', :q, '%'))
           or lower(u.email) like lower(concat('%', :q, '%'))
        """)
    Page<Usuario> buscarConRol(@Param("q") String q, Pageable pageable);

    Optional<Usuario> findByTokenRecuperacion(String token);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    long countByRolCodigoIgnoreCase(String codigo);

    @EntityGraph(attributePaths = {"rol"})
    @Query("""
        select u from Usuario u
        where upper(u.rol.codigo) = upper(:codigo)
        order by u.nombre asc
        """)
    List<Usuario> listarPorRol(@Param("codigo") String codigo);

    @Query("select count(u) from Usuario u where u.activo = true and u.rol.codigo = :codigo")
    long contarActivosPorRol(@Param("codigo") String codigo);
}
