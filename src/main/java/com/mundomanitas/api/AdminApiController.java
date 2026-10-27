package com.mundomanitas.api;

import com.mundomanitas.dto.api.EjercicioDto;
import com.mundomanitas.dto.api.EjercicioRequest;
import com.mundomanitas.dto.api.MensajeResponse;
import com.mundomanitas.dto.api.ModuloDto;
import com.mundomanitas.dto.api.ModuloRequest;
import com.mundomanitas.dto.api.UsuarioDto;
import com.mundomanitas.dto.api.UsuarioRequest;
import com.mundomanitas.dto.api.VideoDto;
import com.mundomanitas.dto.api.VideoRequest;
import com.mundomanitas.service.CursoService;
import com.mundomanitas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;

    public AdminApiController(UsuarioService usuarioService, CursoService cursoService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
    }

    // ------------------------------------------------------------------
    // Usuarios
    // ------------------------------------------------------------------

    @GetMapping("/usuarios")
    public Page<UsuarioDto> listarUsuarios(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return usuarioService.listar(q, page, size);
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioDto obtenerUsuario(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @GetMapping("/roles")
    public List<String> listarRoles() {
        return usuarioService.listarRoles();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDto> crearUsuario(
        @Valid @RequestBody UsuarioRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioService.crear(request));
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioDto actualizarUsuario(
        @PathVariable Integer id,
        @Valid @RequestBody UsuarioRequest request
    ) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/usuarios/{id}/estado")
    public UsuarioDto cambiarEstado(
        @PathVariable Integer id,
        @RequestParam boolean activo
    ) {
        return usuarioService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<MensajeResponse> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminar(id);

        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente."));
    }

    // ------------------------------------------------------------------
    // Módulos
    // ------------------------------------------------------------------

    @GetMapping("/modulos")
    public List<ModuloDto> listarModulos() {
        return cursoService.listarModulosAdmin();
    }

    @PostMapping("/modulos")
    public ResponseEntity<ModuloDto> crearModulo(
        @Valid @RequestBody ModuloRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cursoService.crearModulo(request));
    }

    @PutMapping("/modulos/{id}")
    public ModuloDto actualizarModulo(
        @PathVariable Integer id,
        @Valid @RequestBody ModuloRequest request
    ) {
        return cursoService.actualizarModulo(id, request);
    }

    @DeleteMapping("/modulos/{id}")
    public ResponseEntity<MensajeResponse> eliminarModulo(@PathVariable Integer id) {
        cursoService.eliminarModulo(id);

        return ResponseEntity.ok(new MensajeResponse("Módulo eliminado correctamente."));
    }

    // ------------------------------------------------------------------
    // Videos
    // ------------------------------------------------------------------

    @GetMapping("/modulos/{id}/videos")
    public List<VideoDto> listarVideos(@PathVariable Integer id) {
        return cursoService.listarVideosAdmin(id);
    }

    @PostMapping("/videos")
    public ResponseEntity<VideoDto> crearVideo(
        @Valid @RequestBody VideoRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cursoService.crearVideo(request));
    }

    @PutMapping("/videos/{id}")
    public VideoDto actualizarVideo(
        @PathVariable Integer id,
        @Valid @RequestBody VideoRequest request
    ) {
        return cursoService.actualizarVideo(id, request);
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<MensajeResponse> eliminarVideo(@PathVariable Integer id) {
        cursoService.eliminarVideo(id);

        return ResponseEntity.ok(new MensajeResponse("Video eliminado correctamente."));
    }

    // ------------------------------------------------------------------
    // Ejercicios
    // ------------------------------------------------------------------

    @GetMapping("/modulos/{id}/ejercicios")
    public List<EjercicioDto> listarEjercicios(@PathVariable Integer id) {
        return cursoService.listarEjerciciosAdmin(id);
    }

    @PostMapping("/ejercicios")
    public ResponseEntity<EjercicioDto> crearEjercicio(
        @Valid @RequestBody EjercicioRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cursoService.crearEjercicio(request));
    }

    @PutMapping("/ejercicios/{id}")
    public EjercicioDto actualizarEjercicio(
        @PathVariable Integer id,
        @Valid @RequestBody EjercicioRequest request
    ) {
        return cursoService.actualizarEjercicio(id, request);
    }

    @DeleteMapping("/ejercicios/{id}")
    public ResponseEntity<MensajeResponse> eliminarEjercicio(@PathVariable Integer id) {
        cursoService.eliminarEjercicio(id);

        return ResponseEntity.ok(new MensajeResponse("Ejercicio eliminado correctamente."));
    }
}
