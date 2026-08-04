package faifly.testtask.dto.patient;

import faifly.testtask.dto.visit.VisitDto;
import lombok.Builder;

import java.util.List;

@Builder
public record PatientDto(
        String firstName,
        String lastName,
        List<VisitDto> lastVisits
) {
}
