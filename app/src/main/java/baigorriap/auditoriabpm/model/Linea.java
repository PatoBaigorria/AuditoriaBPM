package baigorriap.auditoriabpm.model;

import java.io.Serializable;
import java.util.Objects;

public class Linea implements Serializable {
    private int idLinea;
    private String descripcion;

    public Linea() {
    }

    public Linea(int idLinea, String descripcion) {
        this.idLinea = idLinea;
        this.descripcion = descripcion;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descrpcion) {
        this.descripcion = descrpcion;
    }
    @Override
    public String toString() {
        return descripcion; // Muestra la descripción en el spinner
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Linea)) return false;
        Linea linea = (Linea) o;
        return idLinea == linea.idLinea;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLinea);
    }
}
