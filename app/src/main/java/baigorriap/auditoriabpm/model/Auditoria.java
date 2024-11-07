package baigorriap.auditoriabpm.model;

import java.io.Serializable;
import java.util.Date;

public class Auditoria implements Serializable {
    private int idAuditoria;
    private int idSupervisor;
    private int idOperario;
    private int idActividad;
    private int idLinea;
    private Date fecha;
    private String comentario;

    public Auditoria() {
    }

    public Auditoria(int idAuditoria, int idSupervisor, int idOperario, int idActividad, int idLinea, Date fecha, String comentario) {
        this.idAuditoria = idAuditoria;
        this.idSupervisor = idSupervisor;
        this.idOperario = idOperario;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.fecha = fecha;
        this.comentario = comentario;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
