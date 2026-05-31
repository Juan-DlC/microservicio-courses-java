package co.edu.uniremington.mscourses.exception;

public class NoAvailableQuotasException extends RuntimeException {
    public NoAvailableQuotasException(Long courseId) {
        super("No hay cupos disponibles para el curso con ID: " + courseId);
    }
}
