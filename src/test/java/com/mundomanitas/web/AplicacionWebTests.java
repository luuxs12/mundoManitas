package com.mundomanitas.web;

import com.mundomanitas.entity.Modulo;
import com.mundomanitas.repository.ModuloRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Autowired
    private ModuloRepository moduloRepository;

    @Test
    void loginEsPublico() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Iniciar sesión")));
    }

    @Test
    void landingEsPublicaParaVisitantes() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cada lección es")));

        mockMvc.perform(get("/inicio"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cada lección es")));
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void landingRedirigeAlDashboardConSesion() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
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
            .andExpect(content().string(containsString("Usuarios de la plataforma")));

        mockMvc.perform(get("/admin/contenido"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Editar módulo")));
    }

    @Test
    @WithUserDetails("admin@mundomanitas.com")
    void elAdministradorPuedeEditarUnModuloYElCambioPersiste() throws Exception {
        mockMvc.perform(post("/admin/modulos")
                .param("titulo", "Módulo temporal CRUD")
                .param("descripcion", "Creado por una prueba")
                .param("nivel", "basico")
                .param("orden", "99")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/contenido"));

        Modulo modulo = moduloRepository.findAllByOrderByOrdenAsc().stream()
            .filter(candidato -> "Módulo temporal CRUD".equals(candidato.getTitulo()))
            .findFirst()
            .orElseThrow();

        mockMvc.perform(post("/admin/modulos/" + modulo.getIdModulo())
                .param("titulo", "Módulo temporal editado")
                .param("descripcion", "Descripción actualizada")
                .param("nivel", "intermedio")
                .param("orden", "98")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/contenido?idModulo=" + modulo.getIdModulo()));

        Modulo actualizado = moduloRepository.findById(modulo.getIdModulo()).orElseThrow();
        assertEquals("Módulo temporal editado", actualizado.getTitulo());
        assertEquals("Descripción actualizada", actualizado.getDescripcion());
        assertEquals("intermedio", actualizado.getNivel());
        assertEquals(98, actualizado.getOrden());

        mockMvc.perform(post("/admin/modulos/" + modulo.getIdModulo() + "/eliminar").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/contenido"));
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
            .andExpect(content().string(containsString("Crear cuenta de estudiante")))
            .andExpect(content().string(containsString("Postular como docente")));

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
    void elFormularioDePostulacionDocenteEsPublico() throws Exception {
        mockMvc.perform(get("/registro/docente"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("POSTULACIÓN DOCENTE")))
            .andExpect(content().string(containsString("Lengua de Señas Peruana")))
            .andExpect(content().string(containsString("experiencia en")))
            .andExpect(content().string(containsString("TI")));
    }

    @Test
    void laPostulacionDocenteRechazaSolicitudesSinRequisitos() throws Exception {
        mockMvc.perform(post("/registro/docente")
                .param("nombre", "Ana")
                .param("email", "postulante.invalido@ejemplo.com")
                .param("telefono", "123")
                .param("nivelLsp", "A1")
                .param("certificacionLsp", "corta")
                .param("aniosExperiencia", "0")
                .param("areaTi", "TI")
                .param("portafolio", "no-es-url")
                .param("motivacion", "muy corta")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Revisa tu postulación")))
            .andExpect(content().string(containsString("al menos 1 año de experiencia en TI")));
    }

    @Test
    void laPostulacionDocenteValidaSeRegistraParaRevision() throws Exception {
        mockMvc.perform(post("/registro/docente")
                .param("nombre", "Rosa Elena Paredes")
                .param("email", "rosa.paredes@ejemplo.com")
                .param("telefono", "987654321")
                .param("nivelLsp", "B2")
                .param("certificacionLsp", "Certificación de LSP nivel B2 - INSOR Perú")
                .param("aniosExperiencia", "3")
                .param("areaTi", "Desarrollo backend")
                .param("portafolio", "https://portafolio.ejemplo.com/rosa")
                .param("motivacion", "Quiero enseñar programación a estudiantes sordos con material "
                    + "visual, claro y accesible, usando la Lengua de Señas Peruana en cada clase.")
                .param("aceptaTerminos", "true")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/registro/docente?enviada=true"));
    }

    @Test
    @WithUserDetails("docente@mundomanitas.com")
    void elDocenteNoPuedeRevisarPostulaciones() throws Exception {
        mockMvc.perform(get("/admin/postulaciones"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@mundomanitas.com")
    void elAdministradorRevisaLasPostulacionesDocentes() throws Exception {
        mockMvc.perform(get("/admin/postulaciones"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Postulaciones docentes")))
            .andExpect(content().string(containsString("Ana Lucía Quispe Ramos")))
            .andExpect(content().string(containsString("Ver portafolio")));
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
