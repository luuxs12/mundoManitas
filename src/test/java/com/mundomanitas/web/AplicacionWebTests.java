package com.mundomanitas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AplicacionWebTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEsPublico() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Iniciar sesión")));
    }

    @Test
    void dashboardRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loginConCredencialesValidasRedirigeAlDashboard() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "alumno@mundomanitas.com")
                .param("password", "123456")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void loginConCredencialesInvalidasMuestraError() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "alumno@mundomanitas.com")
                .param("password", "incorrecta")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void estudiantePuedeVerDashboardCursosYPerfil() throws Exception {
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cursos disponibles")));

        mockMvc.perform(get("/cursos"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Fundamentos de Java")));

        mockMvc.perform(get("/cursos/3"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Introducción a Java")));

        mockMvc.perform(get("/perfil"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Secundaria")));
    }

    @Test
    @WithUserDetails("padre@mundomanitas.com")
    void padrePuedeVerPerfilPeroNoCursos() throws Exception {
        mockMvc.perform(get("/perfil"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/cursos"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void moduloInexistenteDevuelve404ConPaginaPersonalizada() throws Exception {
        mockMvc.perform(get("/cursos/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Página no encontrada")));
    }

    @Test
    @WithUserDetails("docente@mundomanitas.com")
    void docentePuedeGestionarContenidoPeroNoUsuarios() throws Exception {
        mockMvc.perform(get("/admin/contenido"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Módulos, videos y ejercicios")));

        mockMvc.perform(get("/admin/usuarios"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@mundomanitas.com")
    void administradorPuedeGestionarUsuariosYContenido() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cuentas de la plataforma")));

        mockMvc.perform(get("/admin/contenido"))
            .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("sistemas@mundomanitas.com")
    void sistemasPuedeAccederALaAdministracion() throws Exception {
        mockMvc.perform(get("/admin/usuarios"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/admin/contenido"))
            .andExpect(status().isOk());
    }

    @Test
    void laRecuperacionDeContrasenaEsPublicaYValidaElToken() throws Exception {
        mockMvc.perform(get("/recuperar"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Recupera tu acceso")));

        mockMvc.perform(get("/restablecer").param("token", "token-invalido"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/recuperar"));
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void elEstudianteNoDescargaElReporteGeneralPeroSiSuCertificado() throws Exception {
        mockMvc.perform(get("/reportes/progreso.xlsx"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/reportes/certificado/1.docx"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
            ));
    }

    @Test
    @WithUserDetails("docente@mundomanitas.com")
    void elDocenteDescargaElReporteExcel() throws Exception {
        mockMvc.perform(get("/reportes/progreso.xlsx"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(
                org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            ));
    }

    @Test
    void elRegistroEsPublicoYCreaCuentasDeEstudiante() throws Exception {
        mockMvc.perform(get("/registro"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Crear cuenta de estudiante")));

        mockMvc.perform(post("/registro")
                .param("nombre", "Nuevo Estudiante")
                .param("email", "nuevo.estudiante@mundomanitas.com")
                .param("contrasena", "Clave#2026")
                .param("confirmarContrasena", "Clave#2026")
                .param("grado", "Secundaria")
                .param("edad", "16")
                .param("nivelEstudio", "Principiante")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registrado=true"));
    }

    @Test
    @WithUserDetails("maria@mundomanitas.com")
    void elUsuarioPuedeEditarLosDatosDeSuPerfil() throws Exception {
        mockMvc.perform(post("/perfil")
                .param("nombre", "María F. Ríos")
                .param("grado", "Secundaria")
                .param("edad", "17")
                .param("nivelEstudio", "Avanzado")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/perfil"));

        mockMvc.perform(get("/perfil"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("María F. Ríos")));
    }

    @Test
    @WithUserDetails("docente@mundomanitas.com")
    void elDocenteSoloVeSusCursosYNoElPanelGlobal() throws Exception {
        mockMvc.perform(get("/docente/cursos"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cursos a tu cargo")))
            .andExpect(content().string(containsString("Fundamentos de Java")));

        mockMvc.perform(get("/docente/cursos/3"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Avance por estudiante")));

        mockMvc.perform(get("/docente/cursos/999"))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void elEstudianteNoAccedeAlPanelDocenteNiAlAdministrativo() throws Exception {
        mockMvc.perform(get("/docente/cursos"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@mundomanitas.com")
    void elAdministradorVeElPanelGeneral() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Estado de la plataforma")))
            .andExpect(content().string(containsString("Usuarios por rol")));
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void elEstudianteVeSusCursosYElCatalogoDisponible() throws Exception {
        mockMvc.perform(get("/cursos").param("vista", "mis"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Mis cursos")))
            .andExpect(content().string(containsString("Explorar catálogo")));

        mockMvc.perform(get("/cursos").param("vista", "explorar"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Explorar catálogo")));
    }
}
