package baigorriap.auditoriabpm.model;

import java.io.Serializable;
import java.util.Date;

public class Operario implements Serializable {
    private int idOperario;
    private String nombre;
    private String apellido;
    private int legajo;
    private int idActividad;
    private int idLinea;
    private String email;
    private Date ultimaAuditoria;

    public Operario() {
    }

    public Operario(int idOperario, String nombre, String apellido, int legajo, int idActividad, int idLinea, String email) {
        this.idOperario = idOperario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.email = email;
    }

    public Operario(int idOperario, String nombre, String apellido, int legajo, int idActividad, int idLinea, String email, Date ultimaAuditoria) {
        this.idOperario = idOperario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.idActividad = idActividad;
        this.idLinea = idLinea;
        this.email = email;
        this.ultimaAuditoria = ultimaAuditoria;
    }

    public int getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(int idOperario) {
        this.idOperario = idOperario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public int getLegajo() {
        return legajo;
    }

    public void setLegajo(int legajo) {
        this.legajo = legajo;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getUltimaAuditoria() {
        return ultimaAuditoria;
    }

    public void setUltimaAuditoria(Date ultimaAuditoria) {
        this.ultimaAuditoria = ultimaAuditoria;
    }

    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return getNombreCompleto(); // Utiliza el método getNombreCompleto para mostrar en el spinner
    }
}
