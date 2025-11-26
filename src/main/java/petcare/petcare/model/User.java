package petcare.petcare.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un Usuario en el sistema para fines de autenticación y autorización.
 * Almacena credenciales y datos de perfil, incluyendo los de proveedores OAuth.
 */
@Entity
@Table(name = "users") // Se especifica el nombre de la tabla para evitar conflictos con palabras reservadas de SQL.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Identificador único del usuario, generado automáticamente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Correo electrónico del usuario, usado como nombre de usuario. Debe ser único.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Nombre del usuario.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Contraseña del usuario, debe estar encriptada en la base de datos.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Identificador único proporcionado por Google (en caso de login con Google).
     */
    @Column(name = "google_id")
    private String googleId;

    /**
     * URL de la imagen de perfil del usuario.
     */
    private String picture;

    /**
     * Proveedor de autenticación utilizado (por ejemplo, LOCAL, GITHUB, GOOGLE).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    /**
     * Fecha y hora de creación de la cuenta de usuario.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización de los datos del usuario.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
