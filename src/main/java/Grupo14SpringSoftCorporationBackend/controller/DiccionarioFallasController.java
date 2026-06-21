package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.DiccionarioFallas;
import Grupo14SpringSoftCorporationBackend.service.DiccionarioFallasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fallas")
public class DiccionarioFallasController {

    @Autowired
    private DiccionarioFallasService service;

    // REGISTRAR FALLA - TECNICO o JEFE
    @PreAuthorize("hasAnyRole('TECNICO','SISTEMAS')")
    @PostMapping
    public DiccionarioFallas registrar(@RequestBody DiccionarioFallas falla) {
        return service.registrar(falla);
    }

    @GetMapping
    public List<DiccionarioFallas> listar() {
        return service.listar();
    }

    @GetMapping("/buscar")
    public List<DiccionarioFallas> buscar(@RequestParam String keyword) {
        return service.buscar(keyword);
    }
}
