package com.mundomanitas.controller;

import com.mundomanitas.entity.Usuario;
import com.mundomanitas.service.DashboardService;
import com.mundomanitas.service.PanelService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final PanelService panelService;
    private final UsuarioActualService usuarioActual;

    public DashboardController(
        DashboardService dashboardService,
        PanelService panelService,
        UsuarioActualService usuarioActual
    ) {
        this.dashboardService = dashboardService;
        this.panelService = panelService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Map<String, Object> datos = dashboardService.cargar(authentication.getName());
        model.addAllAttributes(datos);

        String tipo = String.valueOf(datos.get("tipo"));

        switch (tipo) {
            case "docente" -> {
                Usuario usuario = usuarioActual.obtener(authentication);
                model.addAttribute("kpis", panelService.kpisDocente(usuario.getIdUsuario()));
                model.addAttribute("cursosDocente", panelService.cursosDocente(usuario.getIdUsuario()));
            }
            case "administrador", "sistemas" -> {
                Map<String, Long> porRol = panelService.usuariosPorRol();
                model.addAttribute("kpis", panelService.kpisGlobales());
                model.addAttribute("cursosGlobales", panelService.cursosGlobales());
                model.addAttribute("usuariosPorRol", porRol);
                model.addAttribute("totalUsuarios", total(porRol));
                model.addAttribute("ultimosUsuarios", panelService.ultimosUsuarios(5));
            }
            default -> {
                // El estudiante y el padre usan las métricas de DashboardService.
            }
        }

        return "dashboard";
    }

    private long total(Map<String, Long> valores) {
        return valores.values().stream().mapToLong(Long::longValue).sum();
    }
}
