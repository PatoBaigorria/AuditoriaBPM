package baigorriap.auditoriabpm.model;

import java.util.List;

public class AltaAuditoriaRequest {
    private int idOperario; // Cambiar auditoria a idOperario
    private int idSupervisor; // Agregar este campo
    private int idActividad; // Agregar este campo
    private int idLinea; // Agregar este campo
    private String comentario; // Agregar este campo
    private List<ItemAuditoriaRequest> items; // Cambiar AuditoriaItemBPM a ItemAuditoriaRequest

    public AltaAuditoriaRequest(int idOperario, int idSupervisor, int idActividad, int idLinea, String comentario, List<ItemAuditoriaRequest> items) {
        this.idOperario = idOperario;
        this.idSupervisor = idSupervisor;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.comentario = comentario;
        this.items = items;
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
}
