package com.mundomanitas.controller;

import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class RecuperacionController {

    private final UsuarioService usuarioService;

    public RecuperacionController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/recuperar")
    public String formulario() {
        return "recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitar(
        @RequestParam String email,
        Model model,
        RedirectAttributes flash
    ) {
        Optional<String> token = usuarioService.solicitarRecuperacion(email);

        // En un entorno real el token viaja por correo electrónico.
        // Aquí se muestra el enlace para poder probar el flujo completo.
        token.ifPresent(valor -> model.addAttribute(
            "enlace",
            "/restablecer?token=" + valor
        ));

        model.addAttribute("email", email);
        model.addAttribute("enviado", true);

        return "recuperar";
    }

    @GetMapping("/restablecer")
    public String restablecerFormulario(
        @RequestParam(required = false) String token,
        Model model,
        RedirectAttributes flash
    ) {
        if (token == null || !usuarioService.tokenVigente(token)) {
            flash.addFlashAttribute(
                "error",
                "El enlace de recuperación no es válido o ya expiró."
            );
            return "redirect:/recuperar";
        }

        model.addAttribute("token", token);
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String restablecer(
        @RequestParam String token,
        @RequestParam String contrasena,
        @RequestParam String confirmacion,
        RedirectAttributes flash
    ) {
        if (!contrasena.equals(confirmacion)) {
            flash.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/restablecer?token=" + token;
        }

        try {
            usuarioService.restablecer(token, contrasena);
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/restablecer?token=" + token;
        }

        flash.addFlashAttribute(
            "exito",
            "Tu contraseña se actualizó. Ya puedes iniciar sesión."
        );

        return "redirect:/login?restablecido=true";
    }
}
