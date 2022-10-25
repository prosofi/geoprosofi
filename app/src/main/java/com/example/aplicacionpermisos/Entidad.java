package com.example.aplicacionpermisos;

public class Entidad {
    private String barrio;
    private String direccion;
    private String latitud;
    private String longitud;
    private String nombre;

    public Entidad(String barrio, String direccion, String latitud, String longitud, String nombre) {
        this.barrio = barrio;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.nombre = nombre;
    }

    public Entidad() {
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
