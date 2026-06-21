package Grupo14SpringSoftCorporationBackend.repository;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    // Tareas asignadas a un técnico
    List<Incidencia> findByIdTecnicoAsignado(Integer idTecnico);

    // Incidencias por estado
    List<Incidencia> findByEstado(String estado);

    // Historial de un equipo
    List<Incidencia> findByCodigoEquipoOrderByFechaRegistroDesc(String codigoEquipo);
}