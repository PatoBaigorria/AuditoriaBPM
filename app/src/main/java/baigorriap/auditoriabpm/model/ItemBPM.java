package baigorriap.auditoriabpm.model;

import java.io.Serializable;

public class ItemBPM implements Serializable {
    private int idItem;
    private String descripcion;

    public ItemBPM() {
    }

    public ItemBPM(int idItem, String descripcion) {
        this.idItem = idItem;
        this.descripcion = descripcion;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
