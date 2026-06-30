package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.repository.UsuarioRepository;
import Grupo14SpringSoftCorporationBackend.util.JwtUtil;
import Grupo14SpringSoftCorporationBackend.model.Incidencia;
import Grupo14SpringSoftCorporationBackend.service.IncidenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(IncidenciaController.class)
@AutoConfigureMockMvc(addFilters = false)
class IncidenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IncidenciaService service;

    private Incidencia incidenciaValida() {
        Incidencia incidencia = new Incidencia();
        incidencia.setCodigoEquipo("EQ-01");
        incidencia.setDescripcionProblema("La pantalla no enciende desde esta mañana");
        return incidencia;
    }

    @Test
    void registrar_devuelveLaIncidenciaCreadaCuandoElCuerpoEsValido() throws Exception {
        Incidencia guardada = incidenciaValida();
        guardada.setIdIncidencia(1);
        guardada.setEstado("Pendiente");
        when(service.registrar(any(Incidencia.class))).thenReturn(guardada);

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incidenciaValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(1))
                .andExpect(jsonPath("$.estado").value("Pendiente"));
    }

    @Test
    void registrar_devuelve400CuandoFaltaElCodigoDeEquipo() throws Exception {
        Incidencia invalida = incidenciaValida();
        invalida.setCodigoEquipo(null);

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.codigoEquipo").exists());
    }

    @Test
    void registrar_devuelve400CuandoLaDescripcionEsDemasiadoCorta() throws Exception {
        Incidencia invalida = incidenciaValida();
        invalida.setDescripcionProblema("corta");

        mockMvc.perform(post("/api/incidencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descripcionProblema").exists());
    }

    @Test
    void listar_devuelveTodasLasIncidencias() throws Exception {
        when(service.listar()).thenReturn(List.of(incidenciaValida(), incidenciaValida()));

        mockMvc.perform(get("/api/incidencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscar_devuelveLaIncidenciaCuandoExiste() throws Exception {
        Incidencia incidencia = incidenciaValida();
        incidencia.setIdIncidencia(5);
        when(service.buscar(5)).thenReturn(incidencia);

        mockMvc.perform(get("/api/incidencias/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idIncidencia").value(5));
    }

    @Test
    void buscar_devuelve404CuandoNoExiste() throws Exception {
        when(service.buscar(99)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no encontrada con id: 99"));

        mockMvc.perform(get("/api/incidencias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Incidencia no encontrada con id: 99"));
    }

    @Test
    void tareasDeTecnico_delegaElIdAlServicio() throws Exception {
        when(service.tareasDeTecnico(3)).thenReturn(List.of(incidenciaValida()));

        mockMvc.perform(get("/api/incidencias/tecnico/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void historialEquipo_delegaElCodigoAlServicio() throws Exception {
        when(service.historialEquipo("EQ-01")).thenReturn(List.of(incidenciaValida()));

        mockMvc.perform(get("/api/incidencias/equipo/EQ-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void asignarTecnico_extraeElIdTecnicoDelCuerpoYLoEnviaAlServicio() throws Exception {
        Incidencia asignada = incidenciaValida();
        asignada.setIdIncidencia(1);
        asignada.setIdTecnicoAsignado(7);
        when(service.asignarTecnico(1, 7)).thenReturn(asignada);

        mockMvc.perform(put("/api/incidencias/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTecnico\": 7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTecnicoAsignado").value(7));
    }

    @Test
    void asignarTecnico_propagaElErrorDeNegocioComo400() throws Exception {
        when(service.asignarTecnico(eq(1), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tecnico no esta disponible."));

        mockMvc.perform(put("/api/incidencias/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idTecnico\": 7}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void solucionar_extraeElTipoDeSolucionDelCuerpo() throws Exception {
        Incidencia solucionada = incidenciaValida();
        solucionada.setIdIncidencia(1);
        solucionada.setEstado("Solucionado");
        solucionada.setTipoSolucion("Cambio de pieza");
        when(service.solucionar(1, "Cambio de pieza")).thenReturn(solucionada);

        mockMvc.perform(put("/api/incidencias/1/solucionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoSolucion\": \"Cambio de pieza\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Solucionado"))
                .andExpect(jsonPath("$.tipoSolucion").value("Cambio de pieza"));
    }

    @Test
    void pendientes_devuelveLasIncidenciasPendientes() throws Exception {
        when(service.pendientes()).thenReturn(List.of(incidenciaValida()));

        mockMvc.perform(get("/api/incidencias/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void solucionadas_devuelveLasIncidenciasSolucionadas() throws Exception {
        when(service.solucionadas()).thenReturn(List.of(incidenciaValida()));

        mockMvc.perform(get("/api/incidencias/solucionadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
