package com.mundomanitas.controller;

import com.mundomanitas.service.CursoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página pública de inicio. Las cuentas autenticadas pasan directo al panel.
 */
@Controller
public class LandingController {

    private final CursoService cursoService;

    public LandingController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping("/")
    public String inicio(Authentication authentication, Model model) {
        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("vitrina", cursoService.listarVitrina(4));
        return "landing";
    }
}
