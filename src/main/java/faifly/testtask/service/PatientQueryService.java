package faifly.testtask.service;

import faifly.testtask.dto.ApiTimeFormat;
import faifly.testtask.dto.doctor.DoctorDto;
import faifly.testtask.dto.patient.PatientDto;
import faifly.testtask.dto.patient.PatientListQuery;
import faifly.testtask.dto.patient.PatientListResponse;
import faifly.testtask.dto.visit.VisitDto;
import faifly.testtask.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static faifly.testtask.repository.PatientPageQuery.PatientVisitRow;

@Service
@RequiredArgsConstructor
public class PatientQueryService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public PatientListResponse getPatients(PatientListQuery query) {
        List<PatientVisitRow> rows = patientRepository.findPageWithLastVisits(
                query.search(), query.doctorIds(), query.page(), query.size());

        List<PatientDto> patients = rows.stream()
                .filter(row -> row.patientId() != null)
                .collect(Collectors.groupingBy(PatientVisitRow::patientId, LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(patientRows -> {
                    List<VisitDto> lastVisits = patientRows.stream()
                            .filter(row -> row.doctorId() != null)
                            .collect(Collectors.toMap(PatientVisitRow::doctorId, Function.identity(),
                                    (r1, r2) -> r1,
                                    LinkedHashMap::new))
                            .values().stream()
                            .map(this::buildVisitDto)
                            .toList();

                    return PatientDto.builder()
                            .firstName(patientRows.getFirst().firstName())
                            .lastName(patientRows.getFirst().lastName())
                            .lastVisits(lastVisits)
                            .build();
                }).toList();

        return new PatientListResponse(patients, rows.getFirst().total().intValue());
    }

    private VisitDto buildVisitDto(PatientVisitRow row) {
        var doctorDto = DoctorDto.builder()
                .firstName(row.doctorFirstName())
                .lastName(row.doctorLastName())
                .totalPatients(row.doctorTotalPatients().intValue())
                .build();

        ZoneId zone = ZoneId.of(row.doctorTimezone());
        return VisitDto.builder()
                .start(ApiTimeFormat.parse(row.start(), zone))
                .end(ApiTimeFormat.parse(row.end(), zone))
                .doctor(doctorDto)
                .build();
    }
}
