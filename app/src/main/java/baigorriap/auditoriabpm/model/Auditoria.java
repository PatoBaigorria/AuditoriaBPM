package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class Auditoria implements Serializable {
    private int idAuditoria;
    private int idSupervisor;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private String fecha;
    private String comentarios;

    public Auditoria() {
    }

    public Auditoria(int idAuditoria, int idSupervisor, int idOperario, int idActividad, int idLinea, String fecha, String comentarios) {
        this.idAuditoria = idAuditoria;
        this.idSupervisor = idSupervisor;
        this.idOperario = idOperario;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.fecha = fecha;
        this.comentarios = comentarios;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(int idSupervisor) {
        this.idSupervisor = idSupervisor;
    }

    public int getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(int idOperario) {
        this.idOperario = idOperario;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
