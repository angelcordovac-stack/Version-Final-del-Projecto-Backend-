package Grupo14SpringSoftCorporationBackend.controller;

import Grupo14SpringSoftCorporationBackend.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para GlobalExceptionHandler, invocando directamente
 * sus metodos @ExceptionHandler (sin necesidad de levantar MockMvc).
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Metodo de utilidad solo para poder construir un MethodParameter valido
    @SuppressWarnings("unused")
    private void metodoDePrueba(String parametro) {
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        Method metodo = GlobalExceptionHandlerTest.class.getDeclaredMethod("metodoDePrueba", String.class);
        return new MethodParameter(metodo, 0);
    }

    @Test
    void handleAccessDenied_devuelve403ConMensajeClaro() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleAccessDenied(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(respuesta.getBody()).containsEntry("status", 403);
        assertThat(respuesta.getBody()).containsEntry("error", "No tiene permisos para realizar esta accion");
    }

    @Test
    void handleMissingParameter_devuelve400IndicandoElParametroFaltante() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("keyword", "String");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleMissingParameter(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("status", 400);
        assertThat(respuesta.getBody().get("error")).isEqualTo("Falta el parametro obligatorio: keyword");
    }

    @Test
    void handleResponseStatus_devuelveElCodigoYElMotivoDeLaExcepcion() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso no encontrado");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleResponseStatus(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).containsEntry("status", 404);
        assertThat(respuesta.getBody()).containsEntry("error", "Recurso no encontrado");
    }

    @Test
    void handleValidation_devuelve400ConElDetalleDeCadaCampoInvalido() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Usuario(), "usuario");
        bindingResult.addError(new FieldError("usuario", "correo", "El correo debe tener un formato válido"));
        bindingResult.addError(new FieldError("usuario", "nombreCompleto", "El nombre completo es obligatorio"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = handler.handleValidation(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("status", 400);
        assertThat(respuesta.getBody()).containsEntry("error", "Error de validación");

        @SuppressWarnings("unchecked")
        Map<String, String> campos = (Map<String, String>) respuesta.getBody().get("campos");
        assertThat(campos).containsEntry("correo", "El correo debe tener un formato válido");
        assertThat(campos).containsEntry("nombreCompleto", "El nombre completo es obligatorio");
    }

    @Test
    void handleValidation_usaMensajePorDefectoCuandoElCampoNoTraeMensaje() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Usuario(), "usuario");
        bindingResult.addError(new FieldError("usuario", "telefono", null));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<Map<String, Object>> respuesta = handler.handleValidation(ex);

        @SuppressWarnings("unchecked")
        Map<String, String> campos = (Map<String, String>) respuesta.getBody().get("campos");
        assertThat(campos).containsEntry("telefono", "Valor inválido");
    }

    @Test
    void handleGeneric_devuelve500ConElMensajeDeLaExcepcion() {
        Exception ex = new RuntimeException("Algo salio mal inesperadamente");

        ResponseEntity<Map<String, Object>> respuesta = handler.handleGeneric(ex);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).containsEntry("status", 500);
        assertThat(respuesta.getBody().get("error")).isEqualTo("Error inesperado: Algo salio mal inesperadamente");
    }
}
