package com.mundomanitas.service;

import com.mundomanitas.dao.CatalogoDao;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Construye las métricas que consume la vista del dashboard según el rol.
 * Toda la lectura ocurre dentro de una transacción para evitar el acceso
 * perezoso fuera de sesión.
 */
@Service
public class DashboardService {

    private final UsuarioDao usuarioDao;
    private final ProgresoDao progresoDao;
    private final CursoDao cursoDao;
    private final CatalogoDao catalogoDao;

    public DashboardService(
        UsuarioDao usuarioDao,
        ProgresoDao progresoDao,
        CursoDao cursoDao,
        CatalogoDao catalogoDao
    ) {
        this.usuarioDao = usuarioDao;
        this.progresoDao = progresoDao;
        this.cursoDao = cursoDao;
        this.catalogoDao = catalogoDao;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> cargar(String email) {
        Usuario usuario = usuarioDao.buscarPorEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Map<String, Object> modelo = new LinkedHashMap<>();
        modelo.put("nombre", usuario.getNombre());
        modelo.put("correo", usuario.getEmail());
        modelo.put("tipo", usuario.getTipo());
        modelo.put("rolNombre", usuario.getRol().getNombre());

        switch (usuario.getTipo()) {
            case "estudiante" -> cargarEstudiante(usuario, modelo);
            case "docente" -> cargarDocente(modelo);
            case "administrador" -> cargarAdministrador(modelo);
            case "sistemas" -> cargarSistemas(modelo);
            default -> {
                // El rol padre y otros roles no requieren métricas propias.
            }
        }

        return modelo;
    }

    private void cargarEstudiante(Usuario usuario, Map<String, Object> modelo) {
        List<Modulo> modulos = cursoDao.listarModulos();

        Map<Integer, Progreso> progresoPorModulo = progresoDao
            .listarPorEstudiante(usuario.getIdUsuario())
            .stream()
            .collect(
                Collectors.toMap(
                    Progreso::getIdModulo,
                    Function.identity(),
                    (primero, segundo) -> primero
                )
            );

        int totalCursos = modulos.size();
        long enProgreso = 0;
        long completados = 0;
        long porIniciar = 0;
        int sumaPorcentajes = 0;

        List<Map<String, Object>> cursosActivos = new ArrayList<>();

        for (Modulo modulo : modulos) {
            Progreso progreso = progresoPorModulo.get(modulo.getIdModulo());
            int porcentaje = porcentaje(progreso);

            sumaPorcentajes += porcentaje;

            if (porcentaje >= 100) {
                completados++;
            } else if (porcentaje > 0) {
                enProgreso++;
            } else {
                porIniciar++;
            }

            if (porcentaje > 0 && porcentaje < 100) {
                Map<String, Object> curso = new HashMap<>();
                curso.put("idModulo", modulo.getIdModulo());
                curso.put("titulo", modulo.getTitulo());
                curso.put("nivel", modulo.getNivel());
                curso.put("porcentaje", porcentaje);
                cursosActivos.add(curso);
            }
        }

        cursosActivos.sort(
            (a, b) -> Integer.compare(
                (Integer) b.get("porcentaje"),
                (Integer) a.get("porcentaje")
            )
        );

        if (cursosActivos.size() > 2) {
            cursosActivos = new ArrayList<>(cursosActivos.subList(0, 2));
        }

        int progresoGeneral = totalCursos == 0
            ? 0
            : (int) Math.round((double) sumaPorcentajes / totalCursos);

        modelo.put("totalCursos", totalCursos);
        modelo.put("enProgreso", enProgreso);
        modelo.put("completados", completados);
        modelo.put("porIniciar", porIniciar);
        modelo.put("progresoGeneral", progresoGeneral);
        modelo.put("cursosActivos", cursosActivos);
    }

    private void cargarDocente(Map<String, Object> modelo) {
        modelo.put("totalModulosEducativos", cursoDao.contarModulos());
        modelo.put("totalVideos", cursoDao.contarVideos());
        modelo.put("totalEjercicios", cursoDao.contarEjercicios());
    }

    private void cargarAdministrador(Map<String, Object> modelo) {
        modelo.put("totalDocentes", usuarioDao.contarPorRol("DOCENTE"));
        modelo.put("totalUsuarios", usuarioDao.contar());
        modelo.put("usuariosActivos", usuarioDao.contarActivos());
    }

    private void cargarSistemas(Map<String, Object> modelo) {
        modelo.put("totalUsuarios", usuarioDao.contar());
        modelo.put("usuariosActivos", usuarioDao.contarActivos());
        modelo.put("totalRoles", catalogoDao.contarRoles());
        modelo.put("totalModulosSistema", catalogoDao.contarModulosSistemaActivos());
    }

    private int porcentaje(Progreso progreso) {
        return progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();
    }
}
