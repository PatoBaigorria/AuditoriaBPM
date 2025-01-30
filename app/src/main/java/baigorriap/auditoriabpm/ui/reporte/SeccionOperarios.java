package baigorriap.auditoriabpm.ui.reporte;

import java.util.List;
import baigorriap.auditoriabpm.model.OperarioSinAuditoria;

public class SeccionOperarios {
    private String titulo;
    private List<OperarioSinAuditoria> operarios;

    public SeccionOperarios(String titulo, List<OperarioSinAuditoria> operarios) {
        this.titulo = titulo;
        this.operarios = operarios;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<OperarioSinAuditoria> getOperarios() {
        return operarios;
    }

    public void setOperarios(List<OperarioSinAuditoria> operarios) {
        this.operarios = operarios;
    }
}
