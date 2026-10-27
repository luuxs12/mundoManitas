package com.mundomanitas.service;

import com.mundomanitas.dao.CatalogoDao;
import com.mundomanitas.dao.UsuarioDao;
import com.mundomanitas.dto.api.UsuarioDto;
import com.mundomanitas.dto.api.UsuarioRequest;
import com.mundomanitas.dto.form.RegistroForm;
import com.mundomanitas.entity.Estudiante;
import com.mundomanitas.entity.Rol;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Reglas de negocio de cuentas de usuario: altas, edición, bloqueo por
 * intentos fallidos y recuperación de contraseña.
 */
@Service
public class UsuarioService {

    private static final int TAMANO_MAXIMO_PAGINA = 50;
    private static final int MINUTOS_VIGENCIA_TOKEN = 30;

    private final UsuarioDao usuarioDao;
    private final CatalogoDao catalogoDao;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    @Value("${app.security.max-intentos:5}")
    private int maxIntentos;

    @Value("${app.security.bloqueo-minutos:15}")
    private int bloqueoMinutos;

    public UsuarioService(
        UsuarioDao usuarioDao,
        CatalogoDao catalogoDao,
        PasswordPolicy passwordPolicy,
        PasswordEncoder passwordEncoder,
        AuditoriaService auditoria
    ) {
        this.usuarioDao = usuarioDao;
        this.catalogoDao = catalogoDao;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<UsuarioDto> listar(String q, int page, int size) {
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), TAMANO_MAXIMO_PAGINA),
            Sort.by(Sort.Direction.ASC, "nombre")
        );

        return usuarioDao.buscar(q, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<String> listarRoles() {
        return catalogoDao.listarRoles().stream()
            .filter(Rol::isActivo)
            .map(Rol::getCodigo)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return Optional.empty();
        }
        return usuarioDao.buscarPorEmail(email.trim());
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return buscarPorEmail(email)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Integer id) {
        return usuarioDao.buscarPorIdConRol(id)
            .map(this::toDto)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioDao.existeEmail(email)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo.");
        }

        if (StringUtils.isBlank(request.contrasena())) {
            throw new ReglaNegocioException("La contraseña es obligatoria para crear el usuario.");
        }

        passwordPolicy.validar(request.contrasena());

        Usuario usuario = new Usuario();
        usuario.setNombre(StringUtils.trim(request.nombre()));
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(buscarRol(request.rol()));
        usuario.setActivo(request.activo() == null || request.activo());
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario guardado = usuarioDao.guardar(usuario);

        auditoria.registrar("USUARIO_CREADO", email, "rol=" + request.rol());

        return toDto(guardado);
    }

    /**
     * Alta pública de una cuenta de estudiante. El rol se fija en el
     * servidor para que el formulario no pueda escalar privilegios.
     */
    @Transactional
    public Usuario registrarEstudiante(RegistroForm form) {
        String email = normalizarEmail(form.getEmail());

        if (StringUtils.isBlank(form.getNombre())) {
            throw new ReglaNegocioException("El nombre es obligatorio.");
        }

        if (usuarioDao.existeEmail(email)) {
            throw new ReglaNegocioException(
                "Ya existe una cuenta registrada con ese correo."
            );
        }

        if (StringUtils.isBlank(form.getContrasena())) {
            throw new ReglaNegocioException("La contraseña es obligatoria.");
        }

        if (!form.getContrasena().equals(form.getConfirmarContrasena())) {
            throw new ReglaNegocioException("Las contraseñas no coinciden.");
        }

        passwordPolicy.validar(form.getContrasena());

        Usuario usuario = new Usuario();
        usuario.setNombre(StringUtils.trim(form.getNombre()));
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(form.getContrasena()));
        usuario.setRol(buscarRol("ESTUDIANTE"));
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario guardado = usuarioDao.guardar(usuario);

        Estudiante perfil = new Estudiante();
        perfil.setIdEstudiante(guardado.getIdUsuario());
        perfil.setGrado(StringUtils.trimToNull(form.getGrado()));
        perfil.setEdad(form.getEdad());
        perfil.setNivelEstudio(StringUtils.trimToNull(form.getNivelEstudio()));
        catalogoDao.guardarEstudiante(perfil);

        auditoria.registrar("USUARIO_REGISTRADO", email, "rol=ESTUDIANTE");

        return guardado;
    }

    @Transactional
    public UsuarioDto actualizar(Integer id, UsuarioRequest request) {
        Usuario usuario = usuarioDao.buscarPorIdConRol(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        String email = normalizarEmail(request.email());

        if (!usuario.getEmail().equalsIgnoreCase(email) && usuarioDao.existeEmail(email)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo.");
        }

        usuario.setNombre(StringUtils.trim(request.nombre()));
        usuario.setEmail(email);
        usuario.setRol(buscarRol(request.rol()));

        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }

        if (StringUtils.isNotBlank(request.contrasena())) {
            passwordPolicy.validar(request.contrasena());
            usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        Usuario guardado = usuarioDao.guardar(usuario);

        auditoria.registrar("USUARIO_ACTUALIZADO", email, "rol=" + request.rol());

        return toDto(guardado);
    }

    @Transactional
    public UsuarioDto cambiarEstado(Integer id, boolean activo) {
        Usuario usuario = usuarioDao.buscarPorIdConRol(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        usuario.setActivo(activo);

        if (activo) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        Usuario guardado = usuarioDao.guardar(usuario);

        auditoria.registrar(
            activo ? "USUARIO_ACTIVADO" : "USUARIO_DESACTIVADO",
            usuario.getEmail(),
            "id=" + id
        );

        return toDto(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Usuario usuario = usuarioDao.buscarPorIdConRol(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        usuarioDao.eliminar(usuario);

        auditoria.registrar("USUARIO_ELIMINADO", usuario.getEmail(), "id=" + id);
    }

    // ------------------------------------------------------------------
    // Bloqueo por intentos fallidos
    // ------------------------------------------------------------------

    @Transactional
    public void registrarIntentoFallido(String email) {
        Optional<Usuario> encontrado = buscarPorEmail(email);

        if (encontrado.isEmpty()) {
            auditoria.registrar("LOGIN_FALLIDO", email, "usuario inexistente");
            return;
        }

        Usuario usuario = encontrado.get();
        usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

        if (usuario.getIntentosFallidos() >= maxIntentos) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(bloqueoMinutos));
            usuario.setIntentosFallidos(0);

            auditoria.registrar(
                "CUENTA_BLOQUEADA",
                email,
                "bloqueo de " + bloqueoMinutos + " minutos"
            );
        } else {
            auditoria.registrar(
                "LOGIN_FALLIDO",
                email,
                "intento " + usuario.getIntentosFallidos() + " de " + maxIntentos
            );
        }

        usuarioDao.guardar(usuario);
    }

    @Transactional
    public void registrarAccesoExitoso(String email) {
        buscarPorEmail(email).ifPresent(usuario -> {
            boolean desbloqueo = usuario.getIntentosFallidos() != 0
                || usuario.getBloqueadoHasta() != null;

            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);

            if (desbloqueo) {
                usuarioDao.guardar(usuario);
            }

            auditoria.registrar("LOGIN_EXITOSO", email, "acceso concedido");
        });
    }

    @Transactional
    public void desbloquearVencidos(String email) {
        buscarPorEmail(email).ifPresent(usuario -> {
            if (usuario.getBloqueadoHasta() != null
                && !usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                usuario.setBloqueadoHasta(null);
                usuario.setIntentosFallidos(0);
                usuarioDao.guardar(usuario);
            }
        });
    }

    // ------------------------------------------------------------------
    // Recuperación de contraseña
    // ------------------------------------------------------------------

    @Transactional
    public Optional<String> solicitarRecuperacion(String email) {
        Optional<Usuario> encontrado = buscarPorEmail(email);

        if (encontrado.isEmpty()) {
            // No se revela si el correo existe o no.
            auditoria.registrar("RECUPERACION_SOLICITADA", email, "correo no registrado");
            return Optional.empty();
        }

        Usuario usuario = encontrado.get();

        String token = UUID.randomUUID().toString().replace("-", "")
            + UUID.randomUUID().toString().replace("-", "");

        usuario.setTokenRecuperacion(token);
        usuario.setTokenRecuperacionExpira(
            LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA_TOKEN)
        );

        usuarioDao.guardar(usuario);

        auditoria.registrar(
            "RECUPERACION_SOLICITADA",
            email,
            "token válido por " + MINUTOS_VIGENCIA_TOKEN + " minutos"
        );

        return Optional.of(token);
    }

    @Transactional
    public void restablecer(String token, String contrasena) {
        passwordPolicy.validar(contrasena);

        Usuario usuario = usuarioDao.buscarPorToken(token)
            .orElseThrow(() -> new ReglaNegocioException(
                "El enlace de recuperación no es válido."
            ));

        if (!usuario.isTokenRecuperacionVigente()) {
            throw new ReglaNegocioException("El enlace de recuperación expiró.");
        }

        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setTokenRecuperacion(null);
        usuario.setTokenRecuperacionExpira(null);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioDao.guardar(usuario);

        auditoria.registrar("CONTRASENA_RESTABLECIDA", usuario.getEmail(), "token consumido");
    }

    @Transactional(readOnly = true)
    public boolean tokenVigente(String token) {
        return usuarioDao.buscarPorToken(token)
            .map(Usuario::isTokenRecuperacionVigente)
            .orElse(false);
    }

    // ------------------------------------------------------------------
    // Mapeos internos
    // ------------------------------------------------------------------

    private Rol buscarRol(String codigo) {
        return catalogoDao.buscarRol(codigo)
            .orElseThrow(() -> new ReglaNegocioException(
                "El rol indicado no existe: " + codigo
            ));
    }

    private String normalizarEmail(String email) {
        return StringUtils.trimToEmpty(email).toLowerCase(Locale.ROOT);
    }

    private UsuarioDto toDto(Usuario usuario) {
        return new UsuarioDto(
            usuario.getIdUsuario(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol() == null ? "" : usuario.getRol().getCodigo(),
            usuario.isActivo()
        );
    }
}
