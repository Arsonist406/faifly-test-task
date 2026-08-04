package faifly.testtask.dto.visit;

import faifly.testtask.dto.doctor.DoctorDto;
import lombok.Builder;

@Builder
public record VisitDto(
        String start,
        String end,
        DoctorDto doctor
) {
}
