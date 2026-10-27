package com.mundomanitas.service;

import com.mundomanitas.dto.form.PostulacionDocenteForm;
import com.mundomanitas.exception.ReglaNegocioException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validación estricta de postulaciones docentes.
 *
 * <p>A diferencia del registro de estudiantes, aquí se exige evidencia
 * verificable: certificación de Lengua de Señas Peruana, experiencia en
 * TI y un portafolio público. Las reglas viven en un componente propio
 * para poder probarlas y reutilizarlas desde la API.</p>
 */
@Component
public class PostulacionValidator {

    private static final Pattern EMAIL = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Set<String> NIVELES_LSP = Set.of("B1", "B2", "C1", "C2");

    private static final int MOTIVACION_MINIMA = 80;
    private static final int MOTIVACION_MAXIMA = 1500;
    private static final int EXPERIENCIA_MINIMA = 1;
    private static final int EXPERIENCIA_MAXIMA = 50;

    /**
     * Valida todos los campos y lanza una sola excepción con el resumen de
     * los problemas encontrados, para que el aspirante pueda corregirlos
     * en una única pasada.
     */
    public void validar(PostulacionDocenteForm form) {
        if (form == null) {
            throw new ReglaNegocioException("No se recibieron los datos de la postulación.");
        }

        List<String> errores = new ArrayList<>();

        validarNombre(form.getNombre(), errores);
        validarEmail(form.getEmail(), errores);
        validarTelefono(form.getTelefono(), errores);
        validarNivelLsp(form.getNivelLsp(), errores);
        validarCertificacion(form.getCertificacionLsp(), errores);
        validarExperiencia(form.getAniosExperiencia(), errores);
        validarAreaTi(form.getAreaTi(), errores);
        validarPortafolio(form.getPortafolio(), errores);
        validarMotivacion(form.getMotivacion(), errores);

        if (!form.isAceptaTerminos()) {
            errores.add("debes aceptar la declaración de veracidad de los datos.");
        }

        if (!errores.isEmpty()) {
            throw new ReglaNegocioException(
                "Revisa tu postulación: " + String.join(" ", errores)
            );
        }
    }

    private void validarNombre(String nombre, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(nombre);

        if (limpio.length() < 5) {
            errores.add("el nombre completo debe tener al menos 5 caracteres.");
            return;
        }

        if (limpio.length() > 100) {
            errores.add("el nombre completo no puede superar los 100 caracteres.");
        }

        if (limpio.split("\\s+").length < 2) {
            errores.add("escribe nombres y apellidos completos.");
        }
    }

    private void validarEmail(String email, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(email);

        if (limpio.isEmpty()) {
            errores.add("el correo electrónico es obligatorio.");
        } else if (limpio.length() > 100 || !EMAIL.matcher(limpio).matches()) {
            errores.add("el correo electrónico no tiene un formato válido.");
        }
    }

    private void validarTelefono(String telefono, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(telefono);
        String digitos = limpio.replaceAll("[^0-9]", "");

        if (digitos.isEmpty()) {
            errores.add("el teléfono de contacto es obligatorio.");
        } else if (digitos.startsWith("51") && digitos.length() == 11) {
            digitos = digitos.substring(2);
        }

        if (!digitos.isEmpty() && (digitos.length() != 9 || !digitos.startsWith("9"))) {
            errores.add("el teléfono debe ser un celular peruano de 9 dígitos (empieza con 9).");
        }
    }

    private void validarNivelLsp(String nivelLsp, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(nivelLsp).toUpperCase(Locale.ROOT);

        if (limpio.isEmpty()) {
            errores.add("debes indicar tu nivel de Lengua de Señas Peruana.");
        } else if (!NIVELES_LSP.contains(limpio)) {
            errores.add("el nivel de LSP debe ser B1, B2, C1 o C2.");
        }
    }

    private void validarCertificacion(String certificacion, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(certificacion);

        if (limpio.length() < 10) {
            errores.add("describe la certificación o institución que acredita tu LSP (mínimo 10 caracteres).");
        } else if (limpio.length() > 200) {
            errores.add("la certificación no puede superar los 200 caracteres.");
        }
    }

    private void validarExperiencia(Integer anios, List<String> errores) {
        if (anios == null) {
            errores.add("indica tus años de experiencia en TI.");
            return;
        }

        if (anios < EXPERIENCIA_MINIMA) {
            errores.add("se requiere al menos 1 año de experiencia en TI.");
        } else if (anios > EXPERIENCIA_MAXIMA) {
            errores.add("los años de experiencia no pueden superar 50.");
        }
    }

    private void validarAreaTi(String areaTi, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(areaTi);

        if (limpio.length() < 3) {
            errores.add("indica tu área de especialidad en TI.");
        } else if (limpio.length() > 120) {
            errores.add("el área de especialidad no puede superar los 120 caracteres.");
        }
    }

    private void validarPortafolio(String portafolio, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(portafolio);

        if (limpio.isEmpty()) {
            errores.add("el enlace al portafolio es obligatorio.");
            return;
        }

        if (limpio.length() > 300) {
            errores.add("el enlace al portafolio no puede superar los 300 caracteres.");
            return;
        }

        try {
            URI uri = new URI(limpio);

            if (uri.getScheme() == null
                || !(uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https"))
                || StringUtils.isBlank(uri.getHost())) {
                errores.add("el portafolio debe ser un enlace http o https válido.");
            }
        } catch (URISyntaxException ex) {
            errores.add("el portafolio debe ser un enlace http o https válido.");
        }
    }

    private void validarMotivacion(String motivacion, List<String> errores) {
        String limpio = StringUtils.trimToEmpty(motivacion);

        if (limpio.length() < MOTIVACION_MINIMA) {
            errores.add(
                "cuéntanos por qué quieres enseñar (mínimo " + MOTIVACION_MINIMA + " caracteres)."
            );
        } else if (limpio.length() > MOTIVACION_MAXIMA) {
            errores.add("la motivación no puede superar los " + MOTIVACION_MAXIMA + " caracteres.");
        }
    }
}
