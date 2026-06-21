package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import Grupo14SpringSoftCorporationBackend.repository.RepuestoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RepuestoService {

    @Autowired
    private RepuestoRepository repo;

    public Repuesto solicitar(Repuesto repuesto) {
        repuesto.setFechaSolicitud(LocalDateTime.now());
        repuesto.setEstado("Solicitado");
        return repo.save(repuesto);
    }

    public List<Repuesto> listar() {
        return repo.findAll();
    }

    public List<Repuesto> solicitados() {
        return repo.findByEstado("Solicitado");
    }

    public List<Repuesto> entregados() {
        return repo.findByEstado("Entregado");
    }

    public Repuesto marcarEntregado(Integer idRepuesto) {
        Repuesto repuesto = repo.findById(idRepuesto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Repuesto no encontrado con id: " + idRepuesto));

        repuesto.setEstado("Entregado");
        repuesto.setFechaEntrega(LocalDateTime.now());
        return repo.save(repuesto);
    }
}
