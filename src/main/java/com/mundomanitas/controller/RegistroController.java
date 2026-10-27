package com.mundomanitas.controller;

import com.mundomanitas.dto.form.RegistroForm;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Alta pública de estudiantes. No requiere sesión iniciada.
 */
@Controller
public class RegistroController {

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String formulario(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
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
            return "registro";
        }
    }
}
