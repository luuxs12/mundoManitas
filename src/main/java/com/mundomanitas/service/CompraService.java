package com.mundomanitas.service;

import com.mundomanitas.dao.CompraDao;
import com.mundomanitas.dao.CursoDao;
import com.mundomanitas.dto.view.CompraView;
import com.mundomanitas.entity.Compra;
import com.mundomanitas.entity.Modulo;
import com.mundomanitas.exception.RecursoNoEncontradoException;
import com.mundomanitas.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Compra simulada de cursos premium.
 *
 * <p>La operación registra el monto, la referencia y la fecha en PostgreSQL y
 * crea la inscripción del estudiante. No se contacta ninguna pasarela de pago:
 * el objetivo académico es demostrar la persistencia y las reglas de negocio
 * alrededor de un curso de pago.</p>
 */
@Service
public class CompraService {

    private static final DateTimeFormatter REFERENCIA =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CompraDao compraDao;
    private final CursoDao cursoDao;
    private final InscripcionService inscripcionService;

    public CompraService(
        CompraDao compraDao,
        CursoDao cursoDao,
        InscripcionService inscripcionService
    ) {
        this.compraDao = compraDao;
        this.cursoDao = cursoDao;
        this.inscripcionService = inscripcionService;
    }

    /**
     * Registra la compra y habilita el curso. Si ya estaba comprado devuelve
     * la compra existente sin duplicar el cobro.
     */
    @Transactional
    public Compra comprar(Integer idEstudiante, Integer idModulo) {
        Modulo modulo = cursoDao.buscarModulo(idModulo)
            .orElseThrow(() -> new RecursoNoEncontradoException("Módulo no encontrado"));

        if (!modulo.isPremium()) {
            throw new ReglaNegocioException("Este curso no requiere compra.");
        }

        BigDecimal precio = modulo.getPrecio() == null
            ? BigDecimal.ZERO
            : modulo.getPrecio();

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El curso premium no tiene un precio válido.");
        }

        Compra existente = compraDao.buscar(idEstudiante, idModulo).orElse(null);

        if (existente != null) {
            inscripcionService.inscribir(idEstudiante, idModulo);
            return existente;
        }

        Compra compra = new Compra();
        compra.setIdEstudiante(idEstudiante);
        compra.setIdModulo(idModulo);
        compra.setMonto(precio);
        compra.setEstado("PAGADA");
        compra.setMetodoPago("SIMULADO");
        compra.setReferencia(referencia(idEstudiante, idModulo));
        compra.setFechaCompra(LocalDateTime.now());

        Compra guardada = compraDao.guardar(compra);

        // La compra incluye el acceso: se crea la inscripción y su progreso.
        inscripcionService.inscribir(idEstudiante, idModulo);

        return guardada;
    }

    @Transactional(readOnly = true)
    public boolean estaComprada(Integer idEstudiante, Integer idModulo) {
        return idEstudiante != null
            && idModulo != null
            && compraDao.existe(idEstudiante, idModulo);
    }

    @Transactional(readOnly = true)
    public List<CompraView> listarPorEstudiante(Integer idEstudiante) {
        return compraDao.listarPorEstudiante(idEstudiante).stream()
            .map(compra -> new CompraView(
                compra.getIdCompra(),
                compra.getIdModulo(),
                cursoDao.buscarModulo(compra.getIdModulo())
                    .map(Modulo::getTitulo)
                    .orElse("Curso"),
                compra.getMonto(),
                compra.getReferencia(),
                compra.getEstado(),
                compra.getFechaCompra()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalInvertido(Integer idEstudiante) {
        BigDecimal total = compraDao.totalInvertido(idEstudiante);
        return total == null ? BigDecimal.ZERO : total;
    }

    private String referencia(Integer idEstudiante, Integer idModulo) {
        return "MM-" + idEstudiante + "-" + idModulo + "-"
            + LocalDateTime.now().format(REFERENCIA);
    }
}
