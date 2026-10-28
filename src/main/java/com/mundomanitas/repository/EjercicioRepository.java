package com.mundomanitas.repository;

import com.mundomanitas.entity.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Integer> {

    List<Ejercicio> findByIdModuloOrderByIdEjercicioAsc(Integer idModulo);

    long countByIdModulo(Integer idModulo);

    boolean existsByIdModuloAndTituloIgnoreCase(Integer idModulo, String titulo);

    void deleteByIdModulo(Integer idModulo);
}
