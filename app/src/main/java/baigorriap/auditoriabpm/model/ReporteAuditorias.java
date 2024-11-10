package baigorriap.auditoriabpm.model;

public class ReporteAuditorias {
    private String mes;
    private int total;
    private int conEstadoNoOk;
    private int conEstadoOk;

    // Constructor, getters y setters
    public ReporteAuditorias(String mes, int total, int conEstadoNoOk, int conEstadoOk) {
        this.mes = mes;
        this.total = total;
        this.conEstadoNoOk = conEstadoNoOk;
        this.conEstadoOk = conEstadoOk;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
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

    public int getConEstadoOk() {
        return conEstadoOk;
    }

    public void setConEstadoOk(int conEstadoOk) {
        this.conEstadoOk = conEstadoOk;
    }
}

