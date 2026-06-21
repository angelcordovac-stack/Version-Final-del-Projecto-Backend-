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
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    @Column(name = "codigo_equipo")
    private String codigoEquipo;

    @Column(name = "descripcion_problema")
    private String descripcionProblema;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "quien_registra")
    private String quienRegistra;

    @Column(name = "id_tecnico_asignado")
    private Integer idTecnicoAsignado;

    @Column(name = "estado")
    private String estado;

    @Column(name = "repuesto_solicitado")
    private String repuestoSolicitado;

    @Column(name = "tipo_solucion")
    private String tipoSolucion;

    @Column(name = "requiere_repuesto")
    private Boolean requiereRepuesto;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_solucion")
    private LocalDateTime fechaSolucion;
}