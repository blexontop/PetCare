package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entidad que representa una Cita veterinaria en el sistema.
 * Asocia una mascota con un veterinario en una fecha y hora específicas.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    /**
     * Identificador único de la cita, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Fecha y hora exactas de la cita. Es un campo obligatorio.
     */
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Motivo principal de la consulta o cita.
     */
    private String motivo;

    /**
     * Estado actual de la cita (por ejemplo, PENDIENTE, CONFIRMADA, CANCELADA).
     * Se almacena como una cadena de texto en la base de datos.
     */
    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    /**
     * Notas o comentarios adicionales del veterinario sobre la cita.
     * @Lob indica que puede ser un campo de texto largo.
     */
    @Lob
    private String notas;

    /**
     * Mascota para la cual se ha agendado la cita.
     * Relación muchos a uno: muchas citas pueden estar asociadas a una mascota.
     * Se carga de forma perezosa (LAZY) y se ignora en la serialización JSON.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Mascota mascota;

    /**
     * Veterinario asignado a la cita.
     * Relación muchos a uno: muchas citas pueden ser atendidas por un veterinario.
     * Se carga de forma perezosa (LAZY) y se ignora en la serialización JSON.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Veterinario veterinario;

    /**
     * Marca de tiempo que registra cuándo se creó la cita.
     */
    private LocalDateTime createdAt;

    
}
