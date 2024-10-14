package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class Firma implements Serializable {
    private int idFirma;
    private int idAuditoria;
    private String datosFirma;
    private String fechaCreacion;

    public Firma() {
    }

    public Firma(int idFirma, int idAuditoria, String datosFirma, String fechaCreacion) {
        this.idFirma = idFirma;
        this.idAuditoria = idAuditoria;
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
