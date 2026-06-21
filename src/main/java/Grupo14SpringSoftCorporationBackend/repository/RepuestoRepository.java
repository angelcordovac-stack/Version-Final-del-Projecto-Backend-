package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    // Repuestos por estado
    List<Repuesto> findByEstado(String estado);

    // Repuestos de una incidencia
    List<Repuesto> findByIdIncidencia(Integer idIncidencia);
}