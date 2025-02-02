package baigorriap.auditoriabpm.model;

import java.util.Date;

public class FirmaPatron {
    private int idFirmaPatron;
    private int idOperario;
    private String firma;           // SVG de la firma
    private String hash;           // Hash de la firma para verificación
    private int puntosTotales;     // Cantidad total de puntos en la firma
    private float velocidadMedia;  // Velocidad media de trazado
    private float presionMedia;    // Presión media aplicada
    private String fechaCreacion;  // Fecha en formato ISO 8601
    private boolean activa;        // Indica si es la firma patrón activa del operario

    public FirmaPatron() {
        // Constructor vacío necesario para Gson
    }

    public FirmaPatron(int idFirmaPatron, int idOperario, String firma, String hash,
                      int puntosTotales, float velocidadMedia, float presionMedia, 
                      String fechaCreacion, boolean activa) {
        this.idFirmaPatron = idFirmaPatron;
        this.idOperario = idOperario;
        this.firma = firma;
        this.hash = hash;
        this.puntosTotales = puntosTotales;
        this.velocidadMedia = velocidadMedia;
        this.presionMedia = presionMedia;
        this.fechaCreacion = fechaCreacion;
        this.activa = activa;
    }

    // Getters y setters
    public int getId() {
        return idFirmaPatron;
    }

    public void setId(int id) {
        this.idFirmaPatron = id;
    }

    public int getIdOperario() {
        return idOperario;
    }

    public void setIdOperario(int idOperario) {
        this.idOperario = idOperario;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }

    public void setPuntosTotales(int puntosTotales) {
        this.puntosTotales = puntosTotales;
    }

    public float getVelocidadMedia() {
        return velocidadMedia;
    }

    public void setVelocidadMedia(float velocidadMedia) {
        this.velocidadMedia = velocidadMedia;
    }

    public float getPresionMedia() {
        return presionMedia;
    }

    public void setPresionMedia(float presionMedia) {
        this.presionMedia = presionMedia;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
