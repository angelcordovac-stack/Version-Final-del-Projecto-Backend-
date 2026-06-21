package Grupo14SpringSoftCorporationBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tecnicos")
public class Tecnico {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "especialidad")
    private String especialidad;

    @Column(name = "max_incidencias")
    private Integer maxIncidencias;

    @Column(name = "disponibilidad")
    private Boolean disponibilidad;
}