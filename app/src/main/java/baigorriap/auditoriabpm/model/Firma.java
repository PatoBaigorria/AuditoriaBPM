package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class Firma implements Serializable {
    private int idFirma;
    private int idAuditoria;
    private boolean noConforme;
    private String datosFirma;
    private String fechaCreacion;  // Fecha en formato ISO 8601

    public Firma() {
    }

    public Firma(int idFirma, int idAuditoria, boolean noConforme, String datosFirma, String fechaCreacion) {
        this.idFirma = idFirma;
        this.idAuditoria = idAuditoria;
        this.noConforme = noConforme;
        this.datosFirma = datosFirma;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(int idFirma) {
        this.idFirma = idFirma;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public boolean isNoConforme() {
        return noConforme;
    }

    public void setNoConforme(boolean noConforme) {
        this.noConforme = noConforme;
    }

    public String getDatosFirma() {
        return datosFirma;
    }

    public void setDatosFirma(String datosFirma) {
        this.datosFirma = datosFirma;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
