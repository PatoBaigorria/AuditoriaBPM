package baigorriap.auditoriabpm.model;

public class ItemAuditoriaRequest {
    private int idItemBPM; // Cambiar el nombre si es necesario
    private String estado; // Cambiar el tipo si usas un enum
    private String comentario; // Este campo es opcional

    public ItemAuditoriaRequest() {
    }

    public ItemAuditoriaRequest(int idItemBPM, String estado, String comentario) {
        this.idItemBPM = idItemBPM;
        this.estado = estado;
        this.comentario = comentario;
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

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
