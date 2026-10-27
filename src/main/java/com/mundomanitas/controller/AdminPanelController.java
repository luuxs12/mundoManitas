package com.mundomanitas.controller;

import com.mundomanitas.entity.Usuario;
import com.mundomanitas.service.PanelService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * Panel general del administrador: monitorea usuarios, cursos,
 * docentes y avance global de la plataforma.
 */
@Controller
public class AdminPanelController {

    private final PanelService panelService;
    private final UsuarioActualService usuarioActual;

    public AdminPanelController(
        PanelService panelService,
        UsuarioActualService usuarioActual
    ) {
        this.panelService = panelService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/admin")
    public String panel(Authentication authentication, Model model) {
        Usuario usuario = usuarioActual.obtener(authentication);
        Map<String, Long> porRol = panelService.usuariosPorRol();

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("kpis", panelService.kpisGlobales());
        model.addAttribute("cursos", panelService.cursosGlobales());
        model.addAttribute("usuariosPorRol", porRol);
        model.addAttribute("totalUsuarios", total(porRol));
        model.addAttribute("ultimosUsuarios", panelService.ultimosUsuarios(6));

        return "admin/panel";
    }

    private long total(Map<String, Long> valores) {
        return valores.values().stream().mapToLong(Long::longValue).sum();
    }
}
