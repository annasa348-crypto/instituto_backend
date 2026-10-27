package com.instituto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "matricula",
       uniqueConstraints = @UniqueConstraint(columnNames = {"alumno_id", "ciclo_lectivo_id"}))
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ciclo_lectivo_id")
    private CicloLectivo cicloLectivo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMatricula estado = EstadoMatricula.PENDIENTE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Alumno getAlumno() { return alumno; }
    public void setAlumno(Alumno alumno) { this.alumno = alumno; }
    public CicloLectivo getCicloLectivo() { return cicloLectivo; }
    public void setCicloLectivo(CicloLectivo cicloLectivo) { this.cicloLectivo = cicloLectivo; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public EstadoMatricula getEstado() { return estado; }
    public void setEstado(EstadoMatricula estado) { this.estado = estado; }
}
