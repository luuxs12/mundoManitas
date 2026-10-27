package com.mundomanitas.controller;

import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.CursoService;
import com.mundomanitas.service.PostulacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Revisión de postulaciones docentes.
 *
 * <p>Solo el administrador y el equipo de sistemas llegan aquí. Aprobar
 * crea la cuenta DOCENTE y asigna un módulo; rechazar exige un motivo.</p>
 */
@Controller
public class AdminPostulacionController {

    private final PostulacionService postulacionService;
    private final CursoService cursoService;

    public AdminPostulacionController(
        PostulacionService postulacionService,
        CursoService cursoService
    ) {
        this.postulacionService = postulacionService;
        this.cursoService = cursoService;
    }

    @GetMapping("/admin/postulaciones")
    public String postulaciones(Model model) {
        model.addAttribute("postulaciones", postulacionService.listar());
        model.addAttribute("pendientes", postulacionService.contarPendientes());
        model.addAttribute("modulos", cursoService.listarModulosAdmin());

        return "admin/postulaciones";
    }

    @PostMapping("/admin/postulaciones/{id}/aprobar")
    public String aprobar(
        @PathVariable Integer id,
        @RequestParam Integer idModulo,
        @RequestParam String contrasena,
        @RequestParam(required = false) String observacion,
        RedirectAttributes flash
    ) {
        try {
            postulacionService.aprobar(id, idModulo, contrasena, observacion);
            flash.addFlashAttribute(
                "exito",
                "Postulación aprobada. Se creó la cuenta DOCENTE y se asignó el módulo."
            );
        } catch (ReglaNegocioException | RecursoNoEncontradoException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/postulaciones";
    }

    @PostMapping("/admin/postulaciones/{id}/rechazar")
    public String rechazar(
        @PathVariable Integer id,
        @RequestParam(required = false) String observacion,
        RedirectAttributes flash
    ) {
        try {
            postulacionService.rechazar(id, observacion);
            flash.addFlashAttribute("exito", "Postulación rechazada y archivada.");
        } catch (ReglaNegocioException | RecursoNoEncontradoException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/postulaciones";
    }
}
