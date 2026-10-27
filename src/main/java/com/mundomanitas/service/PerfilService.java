package com.mundomanitas.service;

import com.mundomanitas.dao.CatalogoDao;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dao.ProgresoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.dto.view.EstudianteView;
import com.mundomanitas.dto.view.PerfilView;
import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Progreso;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Datos del perfil del usuario autenticado, incluyendo el resumen
 * académico cuando se trata de un estudiante.
 */
@Service
public class PerfilService {

    private final UsuarioDao usuarioDao;
    private final CatalogoDao catalogoDao;
    private final ProgresoDao progresoDao;
    private final CursoDao cursoDao;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;

    public PerfilService(
        UsuarioDao usuarioDao,
        CatalogoDao catalogoDao,
        ProgresoDao progresoDao,
        CursoDao cursoDao,
        PasswordEncoder passwordEncoder,
        PasswordPolicy passwordPolicy
    ) {
        this.usuarioDao = usuarioDao;
        this.catalogoDao = catalogoDao;
        this.progresoDao = progresoDao;
        this.cursoDao = cursoDao;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
    }

    @Transactional(readOnly = true)
    public PerfilView cargar(String email) {
        Usuario usuario = usuarioDao.buscarPorEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        boolean esEstudiante = "estudiante".equalsIgnoreCase(usuario.getTipo());

        if (!esEstudiante) {
            return new PerfilView(
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTipo(),
                false,
                null,
                0,
                0,
                0
            );
        }

        EstudianteView estudiante = catalogoDao.buscarEstudiante(usuario.getIdUsuario())
            .map(perfil -> new EstudianteView(
                perfil.getGrado(),
                perfil.getEdad(),
                perfil.getNivelEstudio()
            ))
            .orElse(null);

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

        long completados = 0;
        int sumaPorcentajes = 0;

        for (Modulo modulo : modulos) {
            int porcentaje = porcentaje(progresoPorModulo.get(modulo.getIdModulo()));
            sumaPorcentajes += porcentaje;

            if (porcentaje >= 100) {
                completados++;
            }
        }

        int progresoGeneral = modulos.isEmpty()
            ? 0
            : (int) Math.round((double) sumaPorcentajes / modulos.size());

        return new PerfilView(
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getTipo(),
            true,
            estudiante,
            modulos.size(),
            completados,
            progresoGeneral
        );
    }

    /**
     * Actualiza los datos que el usuario puede cambiar por sí mismo.
     * El correo y el rol permanecen intactos.
     */
    @Transactional
    public void actualizar(
        String email,
        String nombre,
        String grado,
        Integer edad,
        String nivelEstudio
    ) {
        if (StringUtils.isBlank(nombre)) {
            throw new ReglaNegocioException("El nombre no puede quedar vacío.");
        }

        if (edad != null && (edad < 3 || edad > 120)) {
            throw new ReglaNegocioException("La edad debe estar entre 3 y 120 años.");
        }

        Usuario usuario = usuarioDao.buscarPorEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        usuario.setNombre(StringUtils.trim(nombre));
        usuarioDao.guardar(usuario);

        if (!"estudiante".equalsIgnoreCase(usuario.getTipo())) {
            return;
        }

        Estudiante perfil = catalogoDao.buscarEstudiante(usuario.getIdUsuario())
            .orElseGet(() -> {
                Estudiante nuevo = new Estudiante();
                nuevo.setIdEstudiante(usuario.getIdUsuario());
                return nuevo;
            });

        perfil.setGrado(StringUtils.trimToNull(grado));
        perfil.setEdad(edad);
        perfil.setNivelEstudio(StringUtils.trimToNull(nivelEstudio));

        catalogoDao.guardarEstudiante(perfil);
    }

    /**
     * Cambia la contraseña verificando primero la contraseña actual.
     */
    @Transactional
    public void cambiarContrasena(String email, String actual, String nueva, String confirmar) {
        Usuario usuario = usuarioDao.buscarPorEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (StringUtils.isBlank(actual)
            || !passwordEncoder.matches(actual, usuario.getContrasena())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }

        if (StringUtils.isBlank(nueva) || !nueva.equals(confirmar)) {
            throw new ReglaNegocioException("La nueva contraseña y su confirmación no coinciden.");
        }

        passwordPolicy.validar(nueva);

        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioDao.guardar(usuario);
    }

    private int porcentaje(Progreso progreso) {
        return progreso == null || progreso.getPorcentaje() == null
            ? 0
            : progreso.getPorcentaje().intValue();
    }
}
