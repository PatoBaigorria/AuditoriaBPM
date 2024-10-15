package baigorriap.auditoriabpm.model;

import java.io.Serializable;
import java.util.Objects;

public class Actividad implements Serializable {
    private int idActividad;
    private String descripcion;

    public Actividad() {
    }

    public Actividad(int idActividad, String descripcion) {
        this.idActividad = idActividad;
        this.descripcion = descripcion;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    @Override
    public String toString() {
        return descripcion; // Muestra la descripción en el spinner
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actividad)) return false;
        Actividad actividad = (Actividad) o;
        return idActividad == actividad.idActividad; // Comparar por idActividad
    }

    @Override
    public int hashCode() {
        return Objects.hash(idActividad); // Usar idActividad para el hash
    }
}
