package faifly.testtask.dto.patient;

import java.util.List;

public record PatientListResponse(
        List<PatientDto> data,
        int count
) {
}
