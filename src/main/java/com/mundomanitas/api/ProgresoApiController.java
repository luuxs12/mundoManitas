package com.mundomanitas.api;

import com.mundomanitas.dto.api.InscripcionDto;
import com.mundomanitas.dto.api.ProgresoDto;
import com.mundomanitas.service.InscripcionService;
import com.mundomanitas.service.ProgresoService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProgresoApiController {

    private final ProgresoService progresoService;
    private final InscripcionService inscripcionService;
    private final UsuarioActualService usuarioActual;

    public ProgresoApiController(
        ProgresoService progresoService,
        InscripcionService inscripcionService,
        UsuarioActualService usuarioActual
    ) {
        this.progresoService = progresoService;
        this.inscripcionService = inscripcionService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/progreso")
    public List<ProgresoDto> progreso(Authentication authentication) {
        return progresoService.listarPorEstudiante(
            usuarioActual.idEstudiante(authentication)
        );
    }

    @GetMapping("/progreso/general")
    public Map<String, Object> progresoGeneral(Authentication authentication) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put(
            "porcentaje",
            progresoService.progresoGeneral(idEstudiante)
        );

        return respuesta;
    }

    @GetMapping("/inscripciones")
    public List<InscripcionDto> inscripciones(Authentication authentication) {
        return inscripcionService.listarPorEstudiante(
            usuarioActual.idEstudiante(authentication)
        );
    }
}
