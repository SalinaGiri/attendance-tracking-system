package be.ucll.controller.DTO;

import jakarta.validation.constraints.NotBlank;

public record CourseInput(

        String name,

        String description
) {
}
