package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class Linea implements Serializable {
    private int idLinea;
    private String descripcion;

    public Linea() {
    }

    public Linea(int idLinea, String descrpcion) {
        this.idLinea = idLinea;
        this.descripcion = descrpcion;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public String getDescrpcion() {
        return descripcion;
    }

    public void setDescrpcion(String descrpcion) {
        this.descripcion = descrpcion;
    }
    @Override
    public String toString() {
        return descripcion; // Muestra la descripción en el spinner
    }
}
