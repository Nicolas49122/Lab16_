package com.tecsup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha")
    private LocalDateTime fecha;  // Momento en que ocurrió la acción

    @Column(name = "tipo")
    private String tipo;  // Puede ser "INSERT", "UPDATE", "DELETE"

    @Column(name = "accion")
    private String accion;  // Descripción de la acción realizada

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "categoria_nombre")
    private String categoriaNombre;


    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "producto_nombre")
    private String productoNombre;


    @Column(name = "producto_descripcion")
    private String productoDescripcion;

    @Column(name = "producto_precio")
    private String productoPrecio;


    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }



    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }


    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }


    public String getProductoDescripcion() {
        return productoDescripcion;
    }


    public void setProductoDescripcion(String productoDescripcion) {
        this.productoDescripcion = productoDescripcion;
    }



    public String getProductoPrecio() {
        return productoPrecio;
    }


    public void setProductoPrecio(String productoPrecio) {
        this.productoPrecio = productoPrecio;
    }









}
