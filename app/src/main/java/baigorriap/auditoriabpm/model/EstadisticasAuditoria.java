package baigorriap.auditoriabpm.model;

public class EstadisticasAuditoria {
    private int total;
    private int conEstadoNoOk;
    private int conEstadoOK;

    public EstadisticasAuditoria(int total, int conEstadoNoOk, int conEstadoOK) {
        this.total = total;
        this.conEstadoNoOk = conEstadoNoOk;
        this.conEstadoOK = conEstadoOK;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getConEstadoNoOk() {
        return conEstadoNoOk;
    }

    public void setConEstadoNoOk(int conEstadoNoOk) {
        this.conEstadoNoOk = conEstadoNoOk;
    }

    public int getConEstadoOK() {
        return conEstadoOK;
    }

    public void setConEstadoOK(int conEstadoOK) {
        this.conEstadoOK = conEstadoOK;
    }
}
