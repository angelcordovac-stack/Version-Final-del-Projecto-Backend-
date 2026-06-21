package Grupo14SpringSoftCorporationBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "diccionario_fallas")
public class DiccionarioFallas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_falla")
    private Integer idFalla;

    @Column(name = "problema_comun")
    private String problemaComun;

    @Column(name = "solucion_sugerida")
    private String solucionSugerida;

    @Column(name = "id_autor")
    private Integer idAutor;

    // Estado de la solucion: CRITICO, EN_CURSO, MANTENIMIENTO, RESUELTO
    @Column(name = "estado")
    private String estado;

    // Fecha rellenable por el usuario (solo dia, sin hora)
    @Column(name = "fecha")
    private LocalDate fecha;

    // Fecha/hora de registro real en el sistema. Se asigna automaticamente
    // en el backend y nunca se expone como campo editable en el formulario.
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}