package petcare.petcare.model;

/**
 * Enumeración que define los posibles estados de una Cita.
 * Permite gestionar el ciclo de vida de una cita de manera controlada, sin
 * valores diferentes a los definidos.
 * 
 */
public enum EstadoCita {
    /**
     * La cita ha sido solicitada pero aún no ha sido confirmada.
     */
    PENDIENTE,
    /**
     * La cita ha sido aceptada y agendada.
     */
    CONFIRMADA,
    /**
     * La cita ha sido cancelada por el dueño o la clínica.
     */
    CANCELADA,
    /**
     * La cita ya ha ocurrido y se ha completado.
     */
    COMPLETADA
}
