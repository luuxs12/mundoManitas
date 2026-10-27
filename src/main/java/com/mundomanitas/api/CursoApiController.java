package com.mundomanitas.api;

import com.mundomanitas.dto.api.IntentoRequest;
import com.mundomanitas.dto.api.IntentoResponse;
import com.mundomanitas.dto.api.MensajeResponse;
import com.mundomanitas.dto.view.CursoDetalleView;
import com.mundomanitas.dto.view.CursoView;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.service.CursoService;
import com.mundomanitas.service.UsuarioActualService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CursoApiController {

    private final CursoService cursoService;
    private final UsuarioActualService usuarioActual;

    public CursoApiController(
        CursoService cursoService,
        UsuarioActualService usuarioActual
    ) {
        this.cursoService = cursoService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/cursos")
    public Page<CursoView> listar(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "6") int size,
        Authentication authentication
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);
        boolean esEstudiante = "estudiante".equalsIgnoreCase(usuario.getTipo());

        return cursoService.listar(
            q,
            page,
            size,
            esEstudiante ? usuario.getIdUsuario() : null,
            esEstudiante
        );
    }

    @GetMapping("/cursos/{id}")
    public CursoDetalleView detalle(
        @PathVariable Integer id,
        Authentication authentication
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);
        boolean esEstudiante = "estudiante".equalsIgnoreCase(usuario.getTipo());

        return cursoService.detalle(
            id,
            esEstudiante ? usuario.getIdUsuario() : null,
            esEstudiante
        );
    }

    @PostMapping("/cursos/{id}/inscripcion")
    public ResponseEntity<MensajeResponse> inscribirse(
        @PathVariable Integer id,
        Authentication authentication
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        cursoService.inscribir(id, idEstudiante);

        return ResponseEntity.ok(new MensajeResponse(
            "Inscripción registrada correctamente."
        ));
    }

    @DeleteMapping("/cursos/{id}/inscripcion")
    public ResponseEntity<MensajeResponse> cancelarInscripcion(
        @PathVariable Integer id,
        Authentication authentication
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        cursoService.cancelarInscripcion(id, idEstudiante);

        return ResponseEntity.ok(new MensajeResponse(
            "Inscripción cancelada correctamente."
        ));
    }

    @PostMapping("/videos/{id}/completar")
    public ResponseEntity<MensajeResponse> completarVideo(
        @PathVariable Integer id,
        Authentication authentication
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        cursoService.marcarVideoCompletado(idEstudiante, id);

        return ResponseEntity.ok(new MensajeResponse(
            "Video marcado como completado."
        ));
    }

    @PostMapping("/ejercicios/{id}/intento")
    public IntentoResponse responder(
        @PathVariable Integer id,
        @Valid @RequestBody IntentoRequest request,
        Authentication authentication
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        return cursoService.responder(idEstudiante, id, request.respuesta());
    }
}
