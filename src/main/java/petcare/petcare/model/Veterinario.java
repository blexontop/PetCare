package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entidad que representa a un Veterinario o una clínica veterinaria.
 * Contiene información de contacto y su ubicación geográfica.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veterinario {

    /**
     * Identificador único del veterinario, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Nombre del veterinario o de la clínica. Es un campo obligatorio.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Especialidad del veterinario (por ejemplo, "Cirugía", "Animales Exóticos").
     */
    private String especialidad;
    /**
     * Número de teléfono de contacto.
     */
    private String telefono;
    /**
     * Correo electrónico de contacto.
     */
    private String email;
    /**
     * Dirección de la clínica.
     */
    private String direccion;
    /**
     * Ciudad donde se encuentra la clínica.
     */
    private String ciudad;

    /**
     * Coordenada de latitud para la ubicación en un mapa.
     */
    private Double latitud;
    /**
     * Coordenada de longitud para la ubicación en un mapa.
     */
    private Double longitud;

    /**
     * Lista de citas asociadas a este veterinario.
     * Relación uno a muchos: un veterinario puede tener múltiples citas.
     * Se carga de forma perezosa (LAZY) y se ignora en la serialización JSON
     * para evitar referencias circulares.
     */
    @OneToMany(mappedBy = "veterinario", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Cita> citas;
}
