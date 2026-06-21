package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Repuesto;
import Grupo14SpringSoftCorporationBackend.service.RepuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repuestos")
public class RepuestoController {

    @Autowired
    private RepuestoService service;

    // SOLICITAR REPUESTO - TECNICO o JEFE
    @PreAuthorize("hasAnyRole('TECNICO','JEFE')")
    @PostMapping
    public Repuesto solicitar(@RequestBody Repuesto repuesto) {
        return service.solicitar(repuesto);
    }

    @GetMapping
    public List<Repuesto> listar() {
        return service.listar();
    }

    @GetMapping("/solicitados")
    public List<Repuesto> solicitados() {
        return service.solicitados();
    }

    @GetMapping("/entregados")
    public List<Repuesto> entregados() {
        return service.entregados();
    }

    // ENTREGAR - SISTEMAS o JEFE (Logistica)
    @PreAuthorize("hasAnyRole('SISTEMAS','JEFE')")
    @PutMapping("/{id}/entregar")
    public Repuesto marcarEntregado(@PathVariable Integer id) {
        return service.marcarEntregado(id);
    }
}
