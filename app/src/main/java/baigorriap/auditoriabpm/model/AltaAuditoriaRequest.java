package baigorriap.auditoriabpm.model;

import java.util.List;

public class AltaAuditoriaRequest {
    private int idOperario; // Cambiar auditoria a idOperario
    private int idSupervisor; // Agregar este campo
    private int idActividad; // Agregar este campo
    private int idLinea; // Agregar este campo
    private String comentario; // Agregar este campo
    private List<ItemAuditoriaRequest> items; // Cambiar AuditoriaItemBPM a ItemAuditoriaRequest
    private String firma;
    private boolean noConforme;

    public AltaAuditoriaRequest(int idOperario, int idSupervisor, int idActividad, int idLinea, String comentario, List<ItemAuditoriaRequest> items, String firma, boolean noConforme) {
        this.idOperario = idOperario;
        this.idSupervisor = idSupervisor;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.comentario = comentario;
        this.items = items;
        this.firma = firma;
        this.noConforme = noConforme;
    }

    // Getters y setters
    public int getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(int idOperario) {
        this.idOperario = idOperario;
    }

    public int getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(int idSupervisor) {
        this.idSupervisor = idSupervisor;
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

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public List<ItemAuditoriaRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemAuditoriaRequest> items) {
        this.items = items;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public boolean isNoConforme() {
        return noConforme;
    }

    public void setNoConforme(boolean noConforme) {
        this.noConforme = noConforme;
    }
}
