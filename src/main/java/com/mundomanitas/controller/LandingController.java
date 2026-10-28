package com.mundomanitas.controller;

import com.mundomanitas.dto.view.CursoView;
import com.mundomanitas.service.CursoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Página pública de presentación. Los usuarios con sesión activa
 * continúan directamente a su panel.
 */
@Controller
public class LandingController {

    private static final int CURSOS_DESTACADOS = 3;

    private final CursoService cursoService;

    public LandingController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping({"/", "/inicio"})
    public String landing(Authentication authentication, Model model) {
        if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        List<CursoView> cursos = cursoService
            .listar("", 0, CURSOS_DESTACADOS, null, false)
            .getContent();

        model.addAttribute("cursosDestacados", cursos);

        return "landing";
    }
}
