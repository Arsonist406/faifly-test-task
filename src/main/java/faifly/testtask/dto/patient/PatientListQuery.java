package faifly.testtask.dto.patient;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public record PatientListQuery(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String search,
        List<@Positive Integer> doctorIds
) {
    public PatientListQuery {
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
        search = StringUtils.hasText(search) ? search.trim() : null;
        doctorIds = CollectionUtils.isEmpty(doctorIds) ? null : List.copyOf(doctorIds);
    }
}
