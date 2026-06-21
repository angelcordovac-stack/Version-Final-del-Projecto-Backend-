package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import Grupo14SpringSoftCorporationBackend.repository.InformeTecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InformeTecnicoService {

    @Autowired
    private InformeTecnicoRepository repo;

    public InformeTecnico registrar(InformeTecnico informe) {
        informe.setFechaInforme(LocalDateTime.now());
        return repo.save(informe);
    }

    public List<InformeTecnico> listar() {
        return repo.findAll();
    }

    public InformeTecnico buscarPorIncidencia(Integer idIncidencia) {
        return repo.findByIdIncidencia(idIncidencia)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Informe no encontrado para la incidencia: " + idIncidencia));
    }
}
