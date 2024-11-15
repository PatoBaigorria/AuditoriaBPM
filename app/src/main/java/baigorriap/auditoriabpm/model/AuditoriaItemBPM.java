package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class AuditoriaItemBPM implements Serializable {
    private int idAuditoriaItemBPM;
    private int idAuditoria;
    private int idItemBPM;
    private EstadoEnum estado;

    public AuditoriaItemBPM() {
    }

    public AuditoriaItemBPM(int idAuditoriaItemBPM, int idAuditoria, int idItemBPM, EstadoEnum estado) {
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

    public EstadoEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnum estado) {
        this.estado = estado;
    }
    @Override
    public String toString() {
        return "AuditoriaItemBPM{" +
                "idAuditoriaItemBPM=" + idAuditoriaItemBPM +
                ", idAuditoria=" + idAuditoria +
                ", idItemBPM=" + idItemBPM +
                ", estado=" + estado + // Esto asume que 'estado' tiene un método toString()
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
