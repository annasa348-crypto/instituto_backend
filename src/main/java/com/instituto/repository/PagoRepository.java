package com.instituto.repository;

import com.instituto.model.Pago;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByCuotaId(Long cuotaId);

    List<Pago> findByCuotaMatriculaAlumnoId(Long alumnoId);

    // Devuelve null si la cuota todavía no tiene pagos
    @Query("SELECT SUM(p.importe) FROM Pago p WHERE p.cuota.id = :cuotaId")
    BigDecimal sumaPorCuota(@Param("cuotaId") Long cuotaId);
}
