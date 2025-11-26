package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entidad que representa a una Mascota en el sistema.
 * Utiliza Lombok para generar automáticamente getters, setters, constructores,
 * etc.
 * 
 * Varias de las anotaciones de JPA sustituyen a @Data, pero de forma más
 * granular, para un mejor control.
 * 
 * Además, aunque no usemos @Table, JPA es inteligente y aplica una convención
 * de nombrado por defecto.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    /**
     * Identificador único de la mascota, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Nombre de la mascota. Es un campo obligatorio.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Especie de la mascota (por ejemplo, PERRO, GATO, OTRO). Es un campo
     * obligatorio.
     */
    @Column(nullable = false)
    private String especie; // PERRO / GATO / OTRO

    /**
     * Raza de la mascota.
     */
    private String raza;

    /**
     * Fecha de nacimiento de la mascota.
     */
    private LocalDate fechaNacimiento;

    /**
     * Peso de la mascota en kilogramos.
     */
    private Double peso;

    /**
     * URL de una foto de la mascota.
     */
    private String fotoUrl;

    /**
     * Dueño de la mascota.
     * Relación muchos a uno: muchas mascotas pueden pertenecer a un dueño.
     * Se carga de forma perezosa (LAZY) para mejorar el rendimiento.
     * 
     * @JsonIgnore para evitar problemas de serialización en las respuestas JSON
     *             y que no cree un bucle infinito.
     * 
     * @ToString.Exclude y @EqualsAndHashCode sirven para evitar que Lombok incluya
     *                   estas relaciones en los métodos toString, equals y
     *                   hashCode, previniendo
     *                   posibles problemas de rendimiento o bucles infinitos.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dueno_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Dueno dueno;

    /**
     * Lista de citas asociadas a esta mascota.
     * Relación uno a muchos: una mascota puede tener muchas citas.
     * Se carga de forma perezosa (LAZY).
     * 
     * @JsonIgnore para evitar problemas de serialización.
     */
    @OneToMany(mappedBy = "mascota", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Cita> citas;
}
