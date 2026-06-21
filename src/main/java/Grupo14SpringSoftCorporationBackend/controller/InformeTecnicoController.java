package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.InformeTecnico;
import Grupo14SpringSoftCorporationBackend.service.InformeTecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/informes")
public class InformeTecnicoController {

    @Autowired
    private InformeTecnicoService service;

    // REGISTRAR INFORME - solo TECNICO o JEFE
    @PreAuthorize("hasAnyRole('TECNICO','JEFE')")
    @PostMapping
    public InformeTecnico registrar(@RequestBody InformeTecnico informe) {
        return service.registrar(informe);
    }

    @GetMapping
    public List<InformeTecnico> listar() {
        return service.listar();
    }

    @GetMapping("/incidencia/{idIncidencia}")
    public InformeTecnico buscarPorIncidencia(@PathVariable Integer idIncidencia) {
        return service.buscarPorIncidencia(idIncidencia);
    }
}
