package petcare.petcare.model;

/**
 * Enumeración que define los proveedores de autenticación externos soportados por la aplicación.
 * Permite identificar cómo se ha autenticado un usuario (por ejemplo, a través de Google o GitHub).
 */
public enum AuthProvider {
    /**
     * Indica que el usuario se autenticó usando su cuenta de Google.
     */
    GOOGLE,
    /**
     * Indica que el usuario se autenticó usando su cuenta de GitHub.
     */
    GITHUB
}
