package com.mundomanitas.dto.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprobante de una compra para mostrarlo en el panel del estudiante.
 */
public record CompraView(
    Integer idCompra,
    Integer idModulo,
    String curso,
    BigDecimal monto,
    String referencia,
    String estado,
    LocalDateTime fechaCompra
) {
}
