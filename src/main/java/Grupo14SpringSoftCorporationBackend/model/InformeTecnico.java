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
@Table(name = "informes_tecnicos")
public class InformeTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe")
    private Integer idInforme;

    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    @Column(name = "diagnostico")
    private String diagnostico;

    @Column(name = "procedimiento_realizado")
    private String procedimientoRealizado;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_informe")
    private LocalDateTime fechaInforme;
}