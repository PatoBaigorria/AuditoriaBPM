package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class AuditoriaItemBPM implements Serializable {
    private int idAuditoriaItemBPM;
    private int idAuditoria;
    private int idItemBPM;
    private Boolean estado;

    public AuditoriaItemBPM() {
    }

    public AuditoriaItemBPM(int idAuditoriaItemBPM, int idAuditoria, int idItemBPM, Boolean estado) {
        this.idAuditoriaItemBPM = idAuditoriaItemBPM;
        this.idAuditoria = idAuditoria;
        this.idItemBPM = idItemBPM;
        this.estado = estado;
    }

    public int getIdAuditoriaItemBPM() {
        return idAuditoriaItemBPM;
    }

    public void setIdAuditoriaItemBPM(int idAuditoriaItemBPM) {
        this.idAuditoriaItemBPM = idAuditoriaItemBPM;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdItemBPM() {
        return idItemBPM;
    }

    public void setIdItemBPM(int idItemBPM) {
        this.idItemBPM = idItemBPM;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }
}
