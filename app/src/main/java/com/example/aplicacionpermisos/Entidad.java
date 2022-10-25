package com.example.aplicacionpermisos;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Entidad {
    private String barrio;
    private String direccion;
    private String latitud;
    private String longitud;
    private String nombre;
    private String fechaRegistro;
    private String nombreRegistrador;

    public Entidad(String barrio, String direccion, String latitud, String longitud, String nombre, String nombreRegistrador) {
        this.barrio = barrio;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.nombre = nombre;
        this.fechaRegistro = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        this.nombreRegistrador = nombreRegistrador;
    }

    public Entidad() {
        this.barrio = "";
        this.direccion = "";
        this.nombre = "";
        this.latitud = "";
        this.longitud = "";
        this.fechaRegistro = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        this.nombreRegistrador = "";
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getNombreRegistrador() {
        return nombreRegistrador;
    }

    public void setNombreRegistrador(String nombreRegistrador) {
        this.nombreRegistrador = nombreRegistrador;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fecha) {
        this.fechaRegistro = fecha;
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
