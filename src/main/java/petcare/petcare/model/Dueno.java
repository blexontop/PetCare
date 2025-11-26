package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entidad que representa al Dueño de una o más mascotas.
 * Utiliza Lombok para la generación automática de código boilerplate.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dueno {

    /**
     * Identificador único del dueño, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Nombre completo del dueño. Es un campo obligatorio.
     */
    @Column(nullable = false)
    private String nombre;

    /**
     * Correo electrónico del dueño. Debe ser único y es obligatorio.
     * Se utiliza para la comunicación y potencialmente para el login.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Número de teléfono del dueño.
     */
    private String telefono;
    /**
     * Dirección postal del dueño.
     */
    private String direccion;
    /**
     * Ciudad de residencia del dueño.
     */
    private String ciudad;

    /**
     * Lista de mascotas que pertenecen a este dueño.
     * Relación uno a muchos: un dueño puede tener varias mascotas.
     * Se carga de forma perezosa (LAZY) para optimizar el rendimiento.
     * 
     * @JsonIgnore para prevenir bucles infinitos en la serialización a JSON.
     */
    @OneToMany(mappedBy = "dueno", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<Mascota> mascotas;

    /**
     * Campo transitorio para la contraseña durante el proceso de registro.
     * No se persiste en la base de datos en esta entidad,
     * se maneja en la entidad 'User'.
     */
    @Transient
    private String password;
}
