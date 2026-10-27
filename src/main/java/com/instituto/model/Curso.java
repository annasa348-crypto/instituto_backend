package com.instituto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidadMaxima;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ciclo_lectivo_id")
    private CicloLectivo cicloLectivo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public CicloLectivo getCicloLectivo() { return cicloLectivo; }
    public void setCicloLectivo(CicloLectivo cicloLectivo) { this.cicloLectivo = cicloLectivo; }
}
