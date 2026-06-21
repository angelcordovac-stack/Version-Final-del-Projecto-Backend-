package Grupo14SpringSoftCorporationBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @Column(name = "codigo_equipo")
    private String codigoEquipo;

    @Column(name = "marca_modelo")
    private String marcaModelo;

    @Column(name = "area_ubicacion")
    private String areaUbicacion;

    @Column(name = "responsable")
    private String responsable;
}