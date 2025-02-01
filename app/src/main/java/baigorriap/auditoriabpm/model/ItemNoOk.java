package baigorriap.auditoriabpm.model;

import java.util.List;
import java.util.Objects;

public class ItemNoOk {
    private String operario;
    private String descripcion;
    private int count;
    private List<String> comentariosAuditoria;

    public String getOperario() {
        return operario;
    }

    public void setOperario(String operario) {
        this.operario = operario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getComentariosAuditoria() {
        return comentariosAuditoria;
    }

    public void setComentariosAuditoria(List<String> comentariosAuditoria) {
        this.comentariosAuditoria = comentariosAuditoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemNoOk itemNoOk = (ItemNoOk) o;
        return count == itemNoOk.count &&
               Objects.equals(operario, itemNoOk.operario) &&
               Objects.equals(descripcion, itemNoOk.descripcion) &&
               Objects.equals(comentariosAuditoria, itemNoOk.comentariosAuditoria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operario, descripcion, count, comentariosAuditoria);
    }
}
