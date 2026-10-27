package com.mundomanitas.controller;

import com.mundomanitas.dto.form.PostulacionDocenteForm;
import com.mundomanitas.dto.form.RegistroForm;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.PostulacionService;
import com.mundomanitas.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Registro público.
 *
 * <p>La primera pantalla permite elegir el tipo de acceso. El estudiante
 * crea su cuenta de inmediato; el docente no se registra, postula y espera
 * la validación del administrador.</p>
 */
@Controller
public class RegistroController {

    private final UsuarioService usuarioService;
    private final PostulacionService postulacionService;

    public RegistroController(
        UsuarioService usuarioService,
        PostulacionService postulacionService
    ) {
        this.usuarioService = usuarioService;
        this.postulacionService = postulacionService;
    }

    // ------------------------------------------------------------------
    // Selector de tipo de cuenta
    // ------------------------------------------------------------------

    @GetMapping("/registro")
    public String selector() {
        return "registro";
    }

    // ------------------------------------------------------------------
    // Estudiante
    // ------------------------------------------------------------------

    @GetMapping("/registro/estudiante")
    public String formularioEstudiante(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro-estudiante";
    }

    @PostMapping({"/registro", "/registro/estudiante"})
    public String registrarEstudiante(
        @ModelAttribute RegistroForm form,
        Model model,
        RedirectAttributes flash
    ) {
        try {
            usuarioService.registrarEstudiante(form);
            flash.addFlashAttribute(
                "exito",
                "¡Cuenta creada! Ya puedes iniciar sesión con tu correo."
            );
            return "redirect:/login?registrado=true";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registroForm", form);
            return "registro-estudiante";
        }
    }

    // ------------------------------------------------------------------
    // Docente (postulación)
    // ------------------------------------------------------------------

    @GetMapping("/registro/docente")
    public String formularioDocente(Model model) {
        model.addAttribute("postulacionForm", new PostulacionDocenteForm());
        return "registro-docente";
    }

    @PostMapping("/registro/docente")
    public String registrarPostulacion(
        @ModelAttribute PostulacionDocenteForm form,
        Model model
    ) {
        try {
            postulacionService.registrar(form);
            return "redirect:/registro/docente?enviada=true";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("postulacionForm", form);
            return "registro-docente";
        }
    }
}
