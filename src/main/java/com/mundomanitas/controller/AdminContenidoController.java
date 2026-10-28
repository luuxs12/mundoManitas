package com.mundomanitas.controller;

import com.mundomanitas.dto.api.EjercicioRequest;
import com.mundomanitas.dto.api.ModuloRequest;
import com.mundomanitas.dto.api.VideoRequest;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.CursoService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Gestión de contenido compartida por docentes y administración.
 *
 * <p>El docente solo ve y modifica los cursos que tiene asignados; el
 * administrador y sistemas conservan la visión global.</p>
 */
@Controller
public class AdminContenidoController {

    private final CursoService cursoService;
    private final UsuarioActualService usuarioActual;

    public AdminContenidoController(
        CursoService cursoService,
        UsuarioActualService usuarioActual
    ) {
        this.cursoService = cursoService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/admin/contenido")
    public String contenido(
        @RequestParam(required = false) Integer idModulo,
        Authentication authentication,
        Model model
    ) {
        Integer idDocente = idDocenteDe(authentication);
        Usuario usuario = usuarioActual.obtener(authentication);
        var modulos = cursoService.listarModulosAdmin(idDocente);

        Integer seleccionado = idModulo;

        if (seleccionado == null && !modulos.isEmpty()) {
            seleccionado = modulos.get(0).idModulo();
        }

        if (seleccionado != null) {
            cursoService.verificarPropiedad(seleccionado, idDocente);
        }

        final Integer idSeleccionado = seleccionado;

        var moduloSeleccionado = modulos.stream()
            .filter(modulo -> modulo.idModulo().equals(idSeleccionado))
            .findFirst()
            .orElse(null);

        model.addAttribute("modulos", modulos);
        model.addAttribute("idModulo", seleccionado);
        model.addAttribute("moduloSeleccionado", moduloSeleccionado);
        model.addAttribute("soloMisCursos", idDocente != null);
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("tipo", usuario.getTipo());

        if (seleccionado != null) {
            model.addAttribute("videos", cursoService.listarVideosAdmin(seleccionado));
            model.addAttribute("ejercicios", cursoService.listarEjerciciosAdmin(seleccionado));
        }

        return "admin/contenido";
    }

    @PostMapping("/admin/modulos")
    public String crearModulo(
        @RequestParam String titulo,
        @RequestParam(required = false) String descripcion,
        @RequestParam String nivel,
        @RequestParam(required = false) Integer orden,
        @RequestParam(defaultValue = "false") boolean premium,
        @RequestParam(required = false) BigDecimal precio,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.crearModulo(
                new ModuloRequest(titulo, descripcion, nivel, orden, premium, precio),
                idDocenteDe(authentication)
            );
            flash.addFlashAttribute("exito", "Módulo creado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido";
    }

    /**
     * Actualiza un módulo existente desde el panel de contenido.
     *
     * <p>Si la sesión pertenece a un docente, solo puede editar los módulos
     * que tenga asignados; la administración no envía {@code idDocente} y
     * puede editar cualquiera.</p>
     */
    @PostMapping("/admin/modulos/{id}")
    public String actualizarModulo(
        @PathVariable Integer id,
        @RequestParam String titulo,
        @RequestParam(required = false) String descripcion,
        @RequestParam String nivel,
        @RequestParam(required = false) Integer orden,
        @RequestParam(defaultValue = "false") boolean premium,
        @RequestParam(required = false) BigDecimal precio,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.actualizarModulo(
                id,
                new ModuloRequest(titulo, descripcion, nivel, orden, premium, precio),
                idDocenteDe(authentication)
            );
            flash.addFlashAttribute("exito", "Módulo actualizado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + id;
    }

    @PostMapping("/admin/modulos/{id}/eliminar")
    public String eliminarModulo(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.verificarPropiedad(id, idDocenteDe(authentication));
            cursoService.eliminarModulo(id);
            flash.addFlashAttribute("exito", "Módulo eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido";
    }

    @PostMapping("/admin/videos")
    public String crearVideo(
        @RequestParam Integer idModulo,
        @RequestParam String titulo,
        @RequestParam String url,
        @RequestParam(required = false) Integer duracion,
        @RequestParam(required = false) Integer orden,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.crearVideo(new VideoRequest(
                idModulo,
                titulo,
                url,
                duracion,
                orden
            ), idDocenteDe(authentication));
            flash.addFlashAttribute("exito", "Video agregado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    /**
     * Actualiza un video y devuelve al módulo desde el que se editó.
     */
    @PostMapping("/admin/videos/{id}")
    public String actualizarVideo(
        @PathVariable Integer id,
        @RequestParam Integer idModulo,
        @RequestParam String titulo,
        @RequestParam String url,
        @RequestParam(required = false) Integer duracion,
        @RequestParam(required = false) Integer orden,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.actualizarVideo(
                id,
                new VideoRequest(idModulo, titulo, url, duracion, orden),
                idDocenteDe(authentication)
            );
            flash.addFlashAttribute("exito", "Video actualizado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    @PostMapping("/admin/videos/{id}/eliminar")
    public String eliminarVideo(
        @PathVariable Integer id,
        @RequestParam Integer idModulo,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.eliminarVideo(id, idDocenteDe(authentication));
            flash.addFlashAttribute("exito", "Video eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    @PostMapping("/admin/ejercicios")
    public String crearEjercicio(
        @RequestParam Integer idModulo,
        @RequestParam String titulo,
        @RequestParam String tipo,
        @RequestParam(required = false) String contenido,
        @RequestParam(required = false) String respuestaCorrecta,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.crearEjercicio(new EjercicioRequest(
                idModulo,
                titulo,
                tipo,
                contenido,
                respuestaCorrecta
            ), idDocenteDe(authentication));
            flash.addFlashAttribute("exito", "Ejercicio agregado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    /**
     * Actualiza un ejercicio conservando su respuesta correcta.
     */
    @PostMapping("/admin/ejercicios/{id}")
    public String actualizarEjercicio(
        @PathVariable Integer id,
        @RequestParam Integer idModulo,
        @RequestParam String titulo,
        @RequestParam String tipo,
        @RequestParam(required = false) String contenido,
        @RequestParam(required = false) String respuestaCorrecta,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.actualizarEjercicio(
                id,
                new EjercicioRequest(idModulo, titulo, tipo, contenido, respuestaCorrecta),
                idDocenteDe(authentication)
            );
            flash.addFlashAttribute("exito", "Ejercicio actualizado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    @PostMapping("/admin/ejercicios/{id}/eliminar")
    public String eliminarEjercicio(
        @PathVariable Integer id,
        @RequestParam Integer idModulo,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            cursoService.eliminarEjercicio(id, idDocenteDe(authentication));
            flash.addFlashAttribute("exito", "Ejercicio eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/contenido?idModulo=" + idModulo;
    }

    private Integer idDocenteDe(Authentication authentication) {
        Usuario usuario = usuarioActual.obtener(authentication);
        return "docente".equalsIgnoreCase(usuario.getTipo())
            ? usuario.getIdUsuario()
            : null;
    }
}
