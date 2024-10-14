package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class Supervisor implements Serializable {
    private int idSupervisor;
    private String nombre;
    private String apellido;
    private int legajo;
    private String password;

    public Supervisor() {
    }

    public Supervisor(int idSupervisor, String nombre, String apellido, int legajo, String password) {
        this.idSupervisor = idSupervisor;
        this.nombre = nombre;
        this.apellido = apellido;
        this.legajo = legajo;
        this.password = password;
    }

    public int getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(int idSupervisor) {
        this.idSupervisor = idSupervisor;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return apellido + ", " + nombre;
    }
}

