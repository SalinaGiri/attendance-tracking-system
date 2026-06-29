package be.ucll.dto;

public record GroupCountDTO(
        Long id,
        String name,
        Long courseId,
        int studentCount
) {
}
