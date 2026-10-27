package com.mundomanitas.api;

import com.mundomanitas.dto.api.AprobacionPostulacionRequest;
import com.mundomanitas.dto.api.MensajeResponse;
import com.mundomanitas.dto.api.PostulacionResponse;
import com.mundomanitas.dto.api.RechazoPostulacionRequest;
import com.mundomanitas.dto.api.UsuarioDto;
import com.mundomanitas.dto.form.PostulacionDocenteForm;
import com.mundomanitas.dto.view.PostulacionView;
import com.mundomanitas.entity.EstadoPostulacion;
import com.mundomanitas.entity.PostulacionDocente;
import com.mundomanitas.service.PostulacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST del flujo de postulación docente.
 *
 * <p>El alta es pública (igual que el formulario web) y la revisión queda
 * restringida al administrador por la configuración de seguridad.</p>
 */
@RestController
public class PostulacionApiController {

    private final PostulacionService postulacionService;

    public PostulacionApiController(PostulacionService postulacionService) {
        this.postulacionService = postulacionService;
    }

    @PostMapping("/api/auth/postulaciones")
    public ResponseEntity<PostulacionResponse> postular(
        @RequestBody PostulacionDocenteForm form
    ) {
        PostulacionDocente registrada = postulacionService.registrar(form);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(PostulacionResponse.desde(registrada));
    }

    @GetMapping("/api/admin/postulaciones")
    public List<PostulacionView> listar(
        @RequestParam(required = false) String estado
    ) {
        if (estado == null || estado.isBlank()) {
            return postulacionService.listar();
        }

        return postulacionService.listarPorEstado(EstadoPostulacion.desde(estado));
    }

    @GetMapping("/api/admin/postulaciones/{id}")
    public PostulacionView obtener(@PathVariable Integer id) {
        return postulacionService.obtener(id);
    }

    @PostMapping("/api/admin/postulaciones/{id}/aprobar")
    public UsuarioDto aprobar(
        @PathVariable Integer id,
        @Valid @RequestBody AprobacionPostulacionRequest request
    ) {
        return postulacionService.aprobar(
            id,
            request.idModulo(),
            request.contrasena(),
            request.observacion()
        );
    }

    @PostMapping("/api/admin/postulaciones/{id}/rechazar")
    public MensajeResponse rechazar(
        @PathVariable Integer id,
        @Valid @RequestBody RechazoPostulacionRequest request
    ) {
        postulacionService.rechazar(id, request.observacion());

        return new MensajeResponse("Postulación rechazada.");
    }
}
