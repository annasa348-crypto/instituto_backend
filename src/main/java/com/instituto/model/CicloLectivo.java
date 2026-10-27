package com.instituto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ciclo_lectivo")
public class CicloLectivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer anio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
}
