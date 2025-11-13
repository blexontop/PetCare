package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String especialidad;
    private String telefono;
    private String email;
    private String direccion;
    private String ciudad;

    private Double latitud;
    private Double longitud;

    @OneToMany(mappedBy = "veterinario", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Cita> citas;
}
