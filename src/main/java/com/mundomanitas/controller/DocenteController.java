package com.mundomanitas.controller;

import com.mundomanitas.dto.view.AlumnoProgresoView;
import com.mundomanitas.dto.view.CursoDocenteView;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.service.PanelService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Panel del docente: solo ve los cursos que tiene asignados, los
 * alumnos inscritos y el avance de cada uno.
 */
@Controller
@RequestMapping("/docente")
public class DocenteController {

    private final PanelService panelService;
    private final UsuarioActualService usuarioActual;

    public DocenteController(
        PanelService panelService,
        UsuarioActualService usuarioActual
    ) {
        this.panelService = panelService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/cursos")
    public String cursos(Authentication authentication, Model model) {
        Usuario usuario = usuarioActual.obtener(authentication);

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("kpis", panelService.kpisDocente(usuario.getIdUsuario()));
        model.addAttribute("cursos", panelService.cursosDocente(usuario.getIdUsuario()));

        return "docente/cursos";
    }

    @GetMapping("/cursos/{id}")
    public String curso(
        @PathVariable Integer id,
        Authentication authentication,
        Model model
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);

        Modulo modulo = panelService.cursoDeDocente(id, usuario.getIdUsuario());
        CursoDocenteView resumen = panelService.resumenCursoDeDocente(id, usuario.getIdUsuario());
        List<AlumnoProgresoView> alumnos = panelService.alumnosDeCurso(id);

        long sinIniciar = alumnos.stream().filter(a -> a.getPorcentaje() == 0).count();
        long completaron = alumnos.stream().filter(a -> a.getPorcentaje() >= 100).count();

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("modulo", modulo);
        model.addAttribute("resumen", resumen);
        model.addAttribute("alumnos", alumnos);
        model.addAttribute("sinIniciar", sinIniciar);
        model.addAttribute("completaron", completaron);

        return "docente/curso";
    }
}
