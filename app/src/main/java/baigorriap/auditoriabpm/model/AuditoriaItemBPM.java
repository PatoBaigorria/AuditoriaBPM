package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class AuditoriaItemBPM implements Serializable {
    private int idAuditoriaItemBPM;
    private int idAuditoria;
    private int idItemBPM;
    private EstadoEnum estado;
    private String comentario;

    public AuditoriaItemBPM() {
    }

    public AuditoriaItemBPM(int idAuditoriaItemBPM, int idAuditoria, int idItemBPM, EstadoEnum estado, String comentario) {
        this.idAuditoriaItemBPM = idAuditoriaItemBPM;
        this.idAuditoria = idAuditoria;
        this.idItemBPM = idItemBPM;
        this.estado = estado;
        this.comentario = comentario;
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

    public EstadoEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnum estado) {
        this.estado = estado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    @Override
    public String toString() {
        return "AuditoriaItemBPM{" +
                "idAuditoriaItemBPM=" + idAuditoriaItemBPM +
                ", idAuditoria=" + idAuditoria +
                ", idItemBPM=" + idItemBPM +
                ", estado=" + estado + // Esto asume que 'estado' tiene un método toString()
                ", comentario='" + comentario + '\'' +
                '}';
    }
    public enum EstadoEnum {
        OK(1),
        NOOK(2),
        NA(3);

        private final int value;

        EstadoEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static EstadoEnum fromValue(int value) {
            for (EstadoEnum estado : EstadoEnum.values()) {
                if (estado.getValue() == value) {
                    return estado;
                }
            }
            throw new IllegalArgumentException("No existe un EstadoEnum con el valor: " + value);
        }
    }


}
