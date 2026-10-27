package com.mundomanitas.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registro de eventos de negocio y seguridad en el logger AUDITORIA.
 * La salida se escribe en logs/auditoria.log (ver logback-spring.xml).
 */
@Service
public class AuditoriaService {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    public void registrar(String evento, String usuario, String detalle) {
        AUDITORIA.info(
            "evento={} usuario={} detalle={}",
            limpiar(evento),
            limpiar(usuario),
            limpiar(detalle)
        );
    }

    private String limpiar(String valor) {
        return StringEscapeUtils.escapeJava(StringUtils.defaultString(valor));
    }
}
