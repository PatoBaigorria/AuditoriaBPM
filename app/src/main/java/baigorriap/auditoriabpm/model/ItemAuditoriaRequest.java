package baigorriap.auditoriabpm.model;

public class ItemAuditoriaRequest {
    private int idItemBPM; // Cambiar el nombre si es necesario
    private String estado; // Cambiar el tipo si usas un enum

    public ItemAuditoriaRequest() {
    }

    public ItemAuditoriaRequest(int idItemBPM, String estado) {
        this.idItemBPM = idItemBPM;
        this.estado = estado;
    }

    // Getters y setters
    public int getIdItemBPM() {
        return idItemBPM;
    }

    public void setIdItemBPM(int idItemBPM) {
        this.idItemBPM = idItemBPM;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
