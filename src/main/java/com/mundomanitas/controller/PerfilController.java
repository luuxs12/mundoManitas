package com.mundomanitas.controller;

import com.mundomanitas.dto.form.ContrasenaForm;
import com.mundomanitas.dto.form.PerfilForm;
import com.mundomanitas.dto.view.PerfilView;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.PerfilService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        PerfilView perfil = perfilService.cargar(authentication.getName());

        model.addAttribute("nombre", perfil.getNombre());
        model.addAttribute("correo", perfil.getCorreo());
        model.addAttribute("tipo", perfil.getTipo());
        model.addAttribute("esEstudiante", perfil.isEsEstudiante());
        model.addAttribute("estudiante", perfil.getEstudiante());
        model.addAttribute("totalModulos", perfil.getTotalModulos());
        model.addAttribute("completados", perfil.getCompletados());
        model.addAttribute("progresoGeneral", perfil.getProgresoGeneral());

        PerfilForm form = new PerfilForm();
        form.setNombre(perfil.getNombre());

        if (perfil.getEstudiante() != null) {
            form.setGrado(perfil.getEstudiante().getGrado());
            form.setEdad(perfil.getEstudiante().getEdad());
            form.setNivelEstudio(perfil.getEstudiante().getNivelEstudio());
        }

        model.addAttribute("perfilForm", form);
        model.addAttribute("contrasenaForm", new ContrasenaForm());

        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizar(
        Authentication authentication,
        @ModelAttribute PerfilForm form,
        RedirectAttributes flash
    ) {
        try {
            perfilService.actualizar(
                authentication.getName(),
                form.getNombre(),
                form.getGrado(),
                form.getEdad(),
                form.getNivelEstudio()
            );
            flash.addFlashAttribute("exito", "Tus datos se actualizaron correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/perfil";
    }

    @PostMapping("/perfil/contrasena")
    public String cambiarContrasena(
        Authentication authentication,
        @ModelAttribute ContrasenaForm form,
        RedirectAttributes flash
    ) {
        try {
            perfilService.cambiarContrasena(
                authentication.getName(),
                form.getActual(),
                form.getNueva(),
                form.getConfirmar()
            );
            flash.addFlashAttribute("exito", "Tu contraseña se actualizó correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/perfil";
    }
}
