package com.example.myappconi;

public class Equipo {
    private String n_inventario;
    private String n_serie;
    private String tipo;
    private String clase;
    private String marca;
    private String ram;
    private String disco;
    private String procesador;
    private String estado;

    public Equipo(String n_inventario, String n_serie, String tipo, String clase, String marca,
                  String ram, String disco, String procesador, String estado) {
        this.n_inventario = n_inventario;
        this.n_serie = n_serie;
        this.tipo = tipo;
        this.clase = clase;
        this.marca = marca;
        this.ram = ram;
        this.disco = disco;
        this.procesador = procesador;
        this.estado = estado;
    }

    // Getters y Setters
    public String getN_inventario() {
        return n_inventario;
    }

    public void setN_inventario(String n_inventario) {
        this.n_inventario = n_inventario;
    }

    public String getN_serie() {
        return n_serie;
    }

    public void setN_serie(String n_serie) {
        this.n_serie = n_serie;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getDisco() {
        return disco;
    }

    public void setDisco(String disco) {
        this.disco = disco;
    }

    public String getProcesador() {
        return procesador;
    }

    public void setProcesador(String procesador) {
        this.procesador = procesador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
