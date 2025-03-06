package baigorriap.auditoriabpm.model;

import android.util.Log;
import java.io.Serializable;

public class AuditoriaItemBPM implements Serializable {
    private static final String TAG = "AuditoriaItemBPM";
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
        Log.d(TAG, "Estableciendo estado para item " + idItemBPM + ": " + estado);
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "AuditoriaItemBPM{" +
                "idAuditoriaItemBPM=" + idAuditoriaItemBPM +
                ", idAuditoria=" + idAuditoria +
                ", idItemBPM=" + idItemBPM +
                ", estado=" + estado +
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
            Log.d(TAG, "Convirtiendo valor " + value + " a EstadoEnum");
            for (EstadoEnum estado : EstadoEnum.values()) {
                if (estado.getValue() == value) {
                    Log.d(TAG, "Valor " + value + " convertido a " + estado);
                    return estado;
                }
            }
            Log.w(TAG, "No se encontró un EstadoEnum para el valor: " + value);
            throw new IllegalArgumentException("No existe un EstadoEnum con el valor: " + value);
        }
    }
}
