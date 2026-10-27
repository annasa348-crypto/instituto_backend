package com.instituto.repository;

import com.instituto.model.Cuota;
import com.instituto.model.EstadoCuota;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    boolean existsByMatriculaId(Long matriculaId);

    List<Cuota> findByMatriculaIdOrderByNumero(Long matriculaId);

    List<Cuota> findByEstadoOrderByFechaVencimiento(EstadoCuota estado);

    @Query("SELECT c FROM Cuota c WHERE c.matricula.alumno.id = :alumnoId "
         + "ORDER BY c.matricula.cicloLectivo.anio, c.numero")
    List<Cuota> porAlumno(@Param("alumnoId") Long alumnoId);

    // Regla: cuota con vencimiento pasado y sin abonar => VENCIDA
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Cuota c SET c.estado = :vencida "
         + "WHERE c.estado = :pendiente AND c.fechaVencimiento < :hoy")
    int marcarVencidas(@Param("hoy") LocalDate hoy,
                       @Param("pendiente") EstadoCuota pendiente,
                       @Param("vencida") EstadoCuota vencida);
}
