package com.mundomanitas.controller;

import com.mundomanitas.dto.view.AlumnoProgresoView;
import com.mundomanitas.dto.view.CursoDocenteView;
import com.mundomanitas.dto.view.ResenaResumenView;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.ComunidadService;
import com.mundomanitas.service.PanelService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Panel del docente: solo ve los cursos que tiene asignados, los
 * alumnos inscritos y el avance de cada uno.
 */
@Controller
@RequestMapping("/docente")
public class DocenteController {

    private final PanelService panelService;
    private final ComunidadService comunidadService;
    private final UsuarioActualService usuarioActual;

    public DocenteController(
        PanelService panelService,
        ComunidadService comunidadService,
        UsuarioActualService usuarioActual
    ) {
        this.panelService = panelService;
        this.comunidadService = comunidadService;
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
        model.addAttribute("anuncios", comunidadService.anunciosDelCurso(id));

        ResenaResumenView resenas = comunidadService.resenasDeCurso(id, null, false);
        model.addAttribute("resenas", resenas.getResenas());
        model.addAttribute("totalResenas", resenas.getTotal());
        model.addAttribute("promedioResenas", resenas.getPromedioTexto());

        return "docente/curso";
    }

    @PostMapping("/cursos/{id}/anuncios")
    public String publicarAnuncio(
        @PathVariable Integer id,
        @RequestParam String titulo,
        @RequestParam String mensaje,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);

        try {
            comunidadService.publicarAnuncio(
                id,
                usuario.getIdUsuario(),
                false,
                titulo,
                mensaje
            );
            flash.addFlashAttribute("exito", "Anuncio publicado para los estudiantes del curso.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/docente/cursos/" + id + "#anuncios";
    }

    @PostMapping("/cursos/{id}/anuncios/{idAnuncio}/eliminar")
    public String eliminarAnuncio(
        @PathVariable Integer id,
        @PathVariable Integer idAnuncio,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);

        try {
            comunidadService.eliminarAnuncio(idAnuncio, usuario.getIdUsuario(), false);
            flash.addFlashAttribute("exito", "Anuncio eliminado.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/docente/cursos/" + id + "#anuncios";
    }
}
