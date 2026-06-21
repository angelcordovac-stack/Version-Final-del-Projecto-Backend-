package Grupo14SpringSoftCorporationBackend.service;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.repository.DiccionarioFallasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiccionarioFallasService {

    @Autowired
    private DiccionarioFallasRepository repo;

    public DiccionarioFallas registrar(DiccionarioFallas falla) {
        return repo.save(falla);
    }

    public List<DiccionarioFallas> listar() {
        return repo.findAll();
    }

    public List<DiccionarioFallas> buscar(String keyword) {
        return repo.findByProblemaComunContainingIgnoreCaseOrSolucionSugeridaContainingIgnoreCase(
                keyword, keyword);
    }
}