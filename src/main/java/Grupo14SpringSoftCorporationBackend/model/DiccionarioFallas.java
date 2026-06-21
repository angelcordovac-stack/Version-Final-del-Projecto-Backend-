package Grupo14SpringSoftCorporationBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}