package com.mundomanitas.dto.view;

/**
 * Fila del monitoreo global del administrador: curso, docente responsable
 * y métricas agregadas de avance.
 */
public class CursoGlobalView {

    private final Integer idModulo;
    private final String titulo;
    private final String nivel;
    private final String docente;
    private final long inscritos;
    private final int promedio;
    private final long completados;

    public CursoGlobalView(
        Integer idModulo,
        String titulo,
        String nivel,
        String docente,
        long inscritos,
        int promedio,
        long completados
    ) {
        this.idModulo = idModulo;
        this.titulo = titulo;
        this.nivel = nivel;
        this.docente = docente;
        this.inscritos = inscritos;
        this.promedio = promedio;
        this.completados = completados;
    }

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getNivel() {
        return nivel;
    }

    public String getDocente() {
        return docente;
    }

    public long getInscritos() {
        return inscritos;
    }

    public int getPromedio() {
        return promedio;
    }

    public long getCompletados() {
        return completados;
    }

    public int getTasaFinalizacion() {
        return inscritos == 0
            ? 0
            : (int) Math.round((double) completados * 100 / inscritos);
    }
}
