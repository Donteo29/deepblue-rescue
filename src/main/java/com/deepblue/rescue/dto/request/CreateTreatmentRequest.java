package com.deepblue.rescue.dto.request;

import com.deepblue.rescue.domain.TreatmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTreatmentRequest(
        @NotBlank(message = "Animal code is required")
        @Size(max = 50, message = "Animal code cannot exceed 50 characters")
        String animalCode,

        @NotBlank(message = "Specialist code is required")
        @Size(max = 50, message = "Specialist code cannot exceed 50 characters")
        String specialistCode,

        @NotNull(message = "Treatment date is required")
        LocalDateTime performedAt,

        @NotNull(message = "Treatment type is required")
        TreatmentType type,

        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description
) {
}
