package faifly.testtask.dto.visit;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateVisitRequest(

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
        LocalDateTime start,

        @NotNull
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
        LocalDateTime end,

        @NotNull
        @Positive
        Integer patientId,

        @NotNull
        @Positive
        Integer doctorId
) {
    @AssertTrue(message = "end must be strictly after start")
    public boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
