package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class OperarioSinAuditoria implements Serializable {
    private int idOperario;
    private String nombreCompleto;
    private int legajo;
    private int idLinea;
    private String descripcionLinea;
    private String descripcionActividad;

    public OperarioSinAuditoria() {
    }

    public OperarioSinAuditoria(int idOperario, String nombreCompleto, int legajo, int idLinea, String descripcionLinea, String descripcionActividad) {
        this.idOperario = idOperario;
        this.nombreCompleto = nombreCompleto;
        this.legajo = legajo;
        this.idLinea = idLinea;
        this.descripcionLinea = descripcionLinea;
        this.descripcionActividad = descripcionActividad;
    }

    public int getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(int idOperario) {
        this.idOperario = idOperario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getLegajo() {
        return legajo;
    }

    public void setLegajo(int legajo) {
        this.legajo = legajo;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public String getDescripcionLinea() {
        return descripcionLinea;
    }

    public void setDescripcionLinea(String descripcionLinea) {
        this.descripcionLinea = descripcionLinea;
    }

    public String getDescripcionActividad() {
        return descripcionActividad;
    }

    public void setDescripcionActividad(String descripcionActividad) {
        this.descripcionActividad = descripcionActividad;
    }
}
