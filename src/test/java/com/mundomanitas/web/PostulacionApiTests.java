package com.mundomanitas.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato REST del flujo de postulación docente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostulacionApiTests {

    private static final String POSTULACION_VALIDA = """
        {
          "nombre": "Lucía Ramos Vega",
          "email": "lucia.ramos@ejemplo.com",
          "telefono": "987654321",
          "nivelLsp": "C1",
          "certificacionLsp": "Certificación de LSP nivel C1 - INSOR Perú",
          "aniosExperiencia": 4,
          "areaTi": "Desarrollo de software accesible",
          "portafolio": "https://portafolio.ejemplo.com/lucia",
          "motivacion": "Quiero enseñar programación con material visual y en Lengua de Señas Peruana para estudiantes sordos.",
          "aceptaTerminos": true
        }
        """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void laPostulacionPublicaSeRegistraPorApi() throws Exception {
        mockMvc.perform(post("/api/auth/postulaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(POSTULACION_VALIDA))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andExpect(jsonPath("$.email").value("lucia.ramos@ejemplo.com"));
    }

    @Test
    void laApiRechazaPostulacionesSinRequisitos() throws Exception {
        mockMvc.perform(post("/api/auth/postulaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nombre": "Sin Apellido",
                      "email": "sin.requisitos@ejemplo.com",
                      "telefono": "123",
                      "nivelLsp": "A1",
                      "certificacionLsp": "corta",
                      "aniosExperiencia": 0,
                      "areaTi": "TI",
                      "portafolio": "no-es-url",
                      "motivacion": "corta",
                      "aceptaTerminos": false
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @WithUserDetails("admin@mundomanitas.com")
    void elAdministradorListaLasPostulacionesPorApi() throws Exception {
        mockMvc.perform(get("/api/admin/postulaciones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].email").exists());
    }

    @Test
    @WithUserDetails("alumno@mundomanitas.com")
    void elEstudianteNoAccedeALaRevisionPorApi() throws Exception {
        mockMvc.perform(get("/api/admin/postulaciones"))
            .andExpect(status().isForbidden());
    }
}
