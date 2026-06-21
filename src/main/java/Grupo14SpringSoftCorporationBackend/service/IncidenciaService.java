package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import Grupo14SpringSoftCorporationBackend.model.Tecnico;
import Grupo14SpringSoftCorporationBackend.repository.IncidenciaRepository;
import Grupo14SpringSoftCorporationBackend.repository.TecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository repo;

    @Autowired
    private TecnicoRepository tecnicoRepo;

    public Incidencia registrar(Incidencia incidencia) {
        incidencia.setFechaRegistro(LocalDateTime.now());
        incidencia.setEstado("Pendiente");
        return repo.save(incidencia);
    }

    public List<Incidencia> listar() {
        return repo.findAll();
    }

    public Incidencia buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Incidencia no encontrada con id: " + id));
    }

    public List<Incidencia> tareasDeTecnico(Integer idTecnico) {
        return repo.findByIdTecnicoAsignado(idTecnico);
    }

    public List<Incidencia> historialEquipo(String codigoEquipo) {
        return repo.findByCodigoEquipoOrderByFechaRegistroDesc(codigoEquipo);
    }

    /**
     * Asigna un tecnico a una incidencia validando que:
     *  - La incidencia exista.
     *  - El tecnico exista y este disponible.
     *  - El tecnico no haya superado su limite de incidencias activas (Pendiente).
     */
    public Incidencia asignarTecnico(Integer idIncidencia, Integer idTecnico) {
        Incidencia incidencia = repo.findById(idIncidencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Incidencia no encontrada con id: " + idIncidencia));

        Tecnico tecnico = tecnicoRepo.findById(idTecnico)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tecnico no encontrado con id: " + idTecnico));

        if (Boolean.FALSE.equals(tecnico.getDisponibilidad())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tecnico no esta disponible.");
        }

        // Cuantas incidencias Pendientes ya tiene asignadas
        long carga = repo.findByIdTecnicoAsignado(idTecnico).stream()
                .filter(i -> "Pendiente".equalsIgnoreCase(i.getEstado()))
                .count();

        Integer max = tecnico.getMaxIncidencias() != null ? tecnico.getMaxIncidencias() : 5;
        if (carga >= max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tecnico ya alcanzo su limite de " + max + " incidencias pendientes.");
        }

        incidencia.setIdTecnicoAsignado(idTecnico);
        incidencia.setFechaAsignacion(LocalDateTime.now());
        return repo.save(incidencia);
    }

    public Incidencia solucionar(Integer idIncidencia, String tipoSolucion) {
        Incidencia incidencia = repo.findById(idIncidencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Incidencia no encontrada con id: " + idIncidencia));

        incidencia.setEstado("Solucionado");
        incidencia.setTipoSolucion(tipoSolucion);
        incidencia.setFechaSolucion(LocalDateTime.now());
        return repo.save(incidencia);
    }

    public List<Incidencia> pendientes() {
        return repo.findByEstado("Pendiente");
    }

    public List<Incidencia> solucionadas() {
        return repo.findByEstado("Solucionado");
    }
}
