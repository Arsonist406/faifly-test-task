package faifly.testtask.dto.visit;

import lombok.Builder;

@Builder
public record VisitResponse(
        Long id,
        String start,
        String end,
        Integer patientId,
        Integer doctorId
) {
}
