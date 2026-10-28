package com.mundomanitas.controller;

import com.mundomanitas.dto.api.UsuarioDto;
import com.mundomanitas.dto.api.UsuarioRequest;
import com.mundomanitas.entity.Usuario;
import com.mundomanitas.exception.ReglaNegocioException;
import com.mundomanitas.service.UsuarioActualService;
import com.mundomanitas.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioActualService usuarioActual;

    public AdminUsuarioController(
        UsuarioService usuarioService,
        UsuarioActualService usuarioActual
    ) {
        this.usuarioService = usuarioService;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/admin/usuarios")
    public String usuarios(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "0") int page,
        Authentication authentication,
        Model model
    ) {
        Page<UsuarioDto> usuarios = usuarioService.listar(q, page, 10);
        Usuario usuario = usuarioActual.obtener(authentication);

        model.addAttribute("usuarios", usuarios.getContent());
        model.addAttribute("pagina", usuarios.getNumber());
        model.addAttribute("totalPaginas", usuarios.getTotalPages());
        model.addAttribute("totalUsuarios", usuarios.getTotalElements());
        model.addAttribute("q", q);
        model.addAttribute("roles", usuarioService.listarRoles());
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("tipo", usuario.getTipo());

        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios")
    public String crear(
        @RequestParam String nombre,
        @RequestParam String email,
        @RequestParam(required = false) String contrasena,
        @RequestParam String rol,
        @RequestParam(defaultValue = "false") boolean activo,
        RedirectAttributes flash
    ) {
        try {
            usuarioService.crear(new UsuarioRequest(
                nombre,
                email,
                contrasena,
                rol,
                activo
            ));
            flash.addFlashAttribute("exito", "Usuario creado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}")
    public String actualizar(
        @PathVariable Integer id,
        @RequestParam String nombre,
        @RequestParam String email,
        @RequestParam(required = false) String contrasena,
        @RequestParam String rol,
        @RequestParam(defaultValue = "false") boolean activo,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            Usuario actual = usuarioActual.obtener(authentication);
            usuarioService.actualizar(id, new UsuarioRequest(
                nombre,
                email,
                contrasena,
                rol,
                activo
            ), actual.getIdUsuario());
            flash.addFlashAttribute("exito", "Usuario actualizado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/estado")
    public String cambiarEstado(
        @PathVariable Integer id,
        @RequestParam boolean activo,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            Usuario actual = usuarioActual.obtener(authentication);
            usuarioService.cambiarEstado(id, activo, actual.getIdUsuario());

            flash.addFlashAttribute(
                "exito",
                activo ? "Usuario activado." : "Usuario desactivado."
            );
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/eliminar")
    public String eliminar(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes flash
    ) {
        try {
            Usuario actual = usuarioActual.obtener(authentication);
            usuarioService.eliminar(id, actual.getIdUsuario());
            flash.addFlashAttribute("exito", "Usuario eliminado.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/usuarios";
    }
}
