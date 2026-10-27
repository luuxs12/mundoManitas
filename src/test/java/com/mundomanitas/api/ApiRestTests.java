package com.mundomanitas.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la API REST: emisión de JWT, respuestas 401/403 en JSON y
 * acceso a recursos protegidos con el token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiRestTests {

    @Autowired
    private MockMvc mockMvc;

    private String tokenAlumno() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"alumno@mundomanitas.com","password":"123456"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.tipo").value("Bearer"))
            .andReturn();

        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void loginDevuelveUnTokenJwt() throws Exception {
        String token = tokenAlumno();

        org.junit.jupiter.api.Assertions.assertFalse(token.isBlank());
        org.junit.jupiter.api.Assertions.assertEquals(3, token.split("\\.").length);
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"alumno@mundomanitas.com","password":"incorrecta"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    void apiProtegidaSinTokenDevuelve401Json() throws Exception {
        mockMvc.perform(get("/api/cursos"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("No autenticado"));
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void apiAdminConRolEstudianteDevuelve403Json() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    @Test
    void elTokenJwtPermiteConsultarElCatalogo() throws Exception {
        String token = tokenAlumno();

        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].titulo").isNotEmpty());
    }

    @Test
    void unTokenInvalidoNoAutenticaLaPeticion() throws Exception {
        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer token.invalido.falso"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void elDetalleDeUnModuloInexistenteDevuelve404Json() throws Exception {
        String token = tokenAlumno();

        mockMvc.perform(get("/api/cursos/9999")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }

    @Test
    void recuperacionDeContrasenaRespondeSinRevelarSiElCorreoExiste() throws Exception {
        mockMvc.perform(post("/api/auth/recuperar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"correo-inexistente@ejemplo.com"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}
