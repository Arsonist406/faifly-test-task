package faifly.testtask.dto.doctor;

import lombok.Builder;

@Builder
public record DoctorDto(
        String firstName,
        String lastName,
        int totalPatients
) {
}
