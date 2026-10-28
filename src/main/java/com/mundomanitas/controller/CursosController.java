package com.mundomanitas.controller;

import com.mundomanitas.dto.api.IntentoResponse;
import com.mundomanitas.dto.view.CursoDetalleView;
import com.mundomanitas.dto.view.CursoView;
import com.mundomanitas.dto.view.ResenaResumenView;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.CompraService;
import com.mundomanitas.service.ComunidadService;
import com.mundomanitas.service.CursoService;
import com.mundomanitas.service.UsuarioActualService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CursosController {

    private final CursoService cursoService;
    private final CompraService compraService;
    private final ComunidadService comunidadService;
    private final UsuarioActualService usuarioActual;

    public CursosController(
        CursoService cursoService,
        CompraService compraService,
        ComunidadService comunidadService,
        UsuarioActualService usuarioActual
    ) {
        this.cursoService = cursoService;
        this.compraService = compraService;
        this.comunidadService = comunidadService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/cursos")
    public String cursos(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "mis") String vista,
        Authentication authentication,
        Model model
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);
        boolean esEstudiante = "estudiante".equalsIgnoreCase(usuario.getTipo());

        Page<CursoView> cursos;
        String vistaActiva = "todos";

        if (esEstudiante) {
            boolean explorar = "explorar".equalsIgnoreCase(vista);
            boolean favoritos = "favoritos".equalsIgnoreCase(vista);

            if (favoritos) {
                cursos = cursoService.listarFavoritos(usuario.getIdUsuario(), q, page, 6);
                vistaActiva = "favoritos";
            } else if (explorar) {
                cursos = cursoService.listarDisponibles(usuario.getIdUsuario(), q, page, 6);
                vistaActiva = "explorar";
            } else {
                cursos = cursoService.listarInscritos(usuario.getIdUsuario(), q, page, 6);
                vistaActiva = "mis";
            }

            model.addAttribute("totalInscritos", cursoService.contarInscritos(usuario.getIdUsuario()));
            model.addAttribute("totalDisponibles", cursoService.contarDisponibles(usuario.getIdUsuario()));
            model.addAttribute("totalFavoritos", cursoService.contarFavoritos(usuario.getIdUsuario()));
        } else {
            cursos = cursoService.listar(q, page, 6, null, false);
            model.addAttribute("totalFavoritos", 0);
        }

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("cursos", cursos.getContent());
        model.addAttribute("pagina", cursos.getNumber());
        model.addAttribute("totalPaginas", cursos.getTotalPages());
        model.addAttribute("totalCursos", cursos.getTotalElements());
        model.addAttribute("q", q);
        model.addAttribute("vista", vistaActiva);
        model.addAttribute("esEstudiante", esEstudiante);

        return "cursos";
    }

    @GetMapping("/cursos/favoritos")
    public String favoritos(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") int page,
        Authentication authentication,
        Model model
    ) {
        return cursos(q, page, "favoritos", authentication, model);
    }

    @GetMapping("/cursos/{id}")
    public String detalleModulo(
        @PathVariable Integer id,
        Authentication authentication,
        Model model
    ) {
        Usuario usuario = usuarioActual.obtener(authentication);
        boolean esEstudiante = "estudiante".equalsIgnoreCase(usuario.getTipo());

        CursoDetalleView detalle = cursoService.detalle(
            id,
            esEstudiante ? usuario.getIdUsuario() : null,
            esEstudiante
        );

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("correo", usuario.getEmail());
        model.addAttribute("tipo", usuario.getTipo());
        model.addAttribute("modulo", detalle.getModulo());
        model.addAttribute("videos", detalle.getVideos());
        model.addAttribute("ejercicios", detalle.getEjercicios());
        model.addAttribute("porcentaje", detalle.getPorcentaje());
        model.addAttribute("estado", detalle.getEstado());
        model.addAttribute("estadoClase", detalle.getEstadoClase());
        model.addAttribute("calificacion", detalle.getCalificacion());
        model.addAttribute("esEstudiante", detalle.isEsEstudiante());
        model.addAttribute("inscrito", detalle.isInscrito());
        model.addAttribute("totalContenidos", detalle.getTotalContenidos());
        model.addAttribute("totalVideos", detalle.getTotalVideos());
        model.addAttribute("totalEjercicios", detalle.getTotalEjercicios());
        model.addAttribute("totalMinutos", detalle.getTotalMinutos());
        model.addAttribute("siguienteVideo", detalle.getSiguienteVideo());
        model.addAttribute("premium", detalle.isPremium());
        model.addAttribute("precio", detalle.getPrecio());
        model.addAttribute("comprado", detalle.isComprado());
        model.addAttribute("bloqueado", detalle.isBloqueado());

        ResenaResumenView resenas = comunidadService.resenasDeCurso(
            id,
            esEstudiante ? usuario.getIdUsuario() : null,
            esEstudiante
        );

        model.addAttribute("resenas", resenas.getResenas());
        model.addAttribute("totalResenas", resenas.getTotal());
        model.addAttribute("promedioResenas", resenas.getPromedioTexto());
        model.addAttribute("puedeResenar", resenas.isPuedeResenar());
        model.addAttribute("miResena", resenas.getMiResena());
        model.addAttribute("favorito", comunidadService.esFavorito(
            esEstudiante ? usuario.getIdUsuario() : null,
            id
        ));
        model.addAttribute(
            "anuncios",
            detalle.isBloqueado() ? List.of() : comunidadService.anunciosActivos(id)
        );

        return "curso-detalle";
    }

    @PostMapping("/cursos/{id}/comprar")
    public String comprar(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        try {
            compraService.comprar(idEstudiante, id);
            flash.addFlashAttribute(
                "exito",
                "Compra registrada. Ya tienes acceso completo a este curso premium."
            );
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cursos/" + id;
    }

    @PostMapping("/cursos/{id}/inscribirse")
    public String inscribirse(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        try {
            cursoService.inscribir(id, idEstudiante);
            flash.addFlashAttribute("exito", "Te inscribiste al módulo correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cursos/" + id;
    }

    @PostMapping("/cursos/{id}/inscripcion/cancelar")
    public String cancelarInscripcion(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        cursoService.cancelarInscripcion(id, idEstudiante);

        flash.addFlashAttribute("exito", "Cancelaste la inscripción al módulo.");

        return "redirect:/cursos";
    }

    @PostMapping("/cursos/{id}/videos/{idVideo}/completar")
    public String completarVideo(
        @PathVariable Integer id,
        @PathVariable Integer idVideo,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        cursoService.marcarVideoCompletado(idEstudiante, idVideo);

        flash.addFlashAttribute("exito", "¡Bien! Marcamos el video como completado.");

        return "redirect:/cursos/" + id;
    }

    @PostMapping("/cursos/{id}/ejercicios/{idEjercicio}/responder")
    public String responderEjercicio(
        @PathVariable Integer id,
        @PathVariable Integer idEjercicio,
        @RequestParam String respuesta,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        IntentoResponse resultado = cursoService.responder(
            idEstudiante,
            idEjercicio,
            respuesta
        );

        if (resultado.correcto()) {
            flash.addFlashAttribute("exito", resultado.mensaje());
        } else {
            flash.addFlashAttribute("error", resultado.mensaje());
        }

        return "redirect:/cursos/" + id;
    }

    // ------------------------------------------------------------------
    // Favoritos y reseñas
    // ------------------------------------------------------------------

    @PostMapping("/cursos/{id}/favorito")
    public String alternarFavorito(
        @PathVariable Integer id,
        @RequestParam(defaultValue = "detalle") String origen,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);
        boolean agregado = comunidadService.alternarFavorito(idEstudiante, id);

        flash.addFlashAttribute(
            "exito",
            agregado
                ? "Curso guardado en tus favoritos."
                : "Curso retirado de tus favoritos."
        );

        return switch (origen) {
            case "catalogo" -> "redirect:/cursos";
            case "favoritos" -> "redirect:/cursos/favoritos";
            default -> "redirect:/cursos/" + id;
        };
    }

    @PostMapping("/cursos/{id}/resena")
    public String guardarResena(
        @PathVariable Integer id,
        @RequestParam Integer calificacion,
        @RequestParam String comentario,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        try {
            comunidadService.guardarResena(idEstudiante, id, calificacion, comentario);
            flash.addFlashAttribute("exito", "Gracias por compartir tu reseña.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cursos/" + id + "#resenas";
    }

    @PostMapping("/cursos/{id}/resenas/{idResena}/eliminar")
    public String eliminarResena(
        @PathVariable Integer id,
        @PathVariable Integer idResena,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        Integer idEstudiante = usuarioActual.idEstudiante(authentication);

        try {
            comunidadService.eliminarResena(idEstudiante, idResena);
            flash.addFlashAttribute("exito", "Tu reseña fue eliminada.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/cursos/" + id + "#resenas";
    }
}
