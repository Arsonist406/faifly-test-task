package faifly.testtask.service;

import faifly.testtask.dto.ApiTimeFormat;
import faifly.testtask.dto.doctor.DoctorDto;
import faifly.testtask.dto.patient.PatientDto;
import faifly.testtask.dto.patient.PatientListQuery;
import faifly.testtask.dto.patient.PatientListResponse;
import faifly.testtask.dto.visit.VisitDto;
import faifly.testtask.entity.Doctor;
import faifly.testtask.entity.Patient;
import faifly.testtask.entity.Visit;
import faifly.testtask.repository.PatientRepository;
import faifly.testtask.repository.VisitRepository;
import faifly.testtask.repository.spec.PatientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static faifly.testtask.repository.VisitRepository.DoctorPatientsCount;

@Service
@RequiredArgsConstructor
public class PatientQueryService {

    private static final Sort ORDER = Sort.by("lastName", "firstName", "id");

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public PatientListResponse getPatients(PatientListQuery query) {
        Specification<Patient> spec = PatientSpecifications.matching(query.search(), query.doctorIds());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), ORDER);
        Page<Patient> page = patientRepository.findAll(spec, pageRequest);

        if (page.isEmpty()) {
            return new PatientListResponse(List.of(), (int) page.getTotalElements());
        }

        List<Integer> patientsIds = page.getContent().stream()
                .map(Patient::getId)
                .toList();
        List<Visit> visits = query.doctorIds() == null
                ? visitRepository.findLastVisitToEachDoctorByPatients(patientsIds)
                : visitRepository.findLastVisitByPatientsAndDoctors(patientsIds, query.doctorIds());

        Map<Integer, Map<Integer, Visit>> patientsLastVisits = visits.stream()
                .collect(Collectors.groupingBy(
                        visit -> visit.getPatient().getId(),
                        LinkedHashMap::new,
                        Collectors.toMap(
                                visit -> visit.getDoctor().getId(),
                                Function.identity(),
                                (v1, v2) -> v1.getStartDateTime().isAfter(v2.getStartDateTime()) ? v1 : v2,
                                LinkedHashMap::new)));

        List<Integer> doctorsIds = visits.stream()
                .map(Visit::getDoctor)
                .map(Doctor::getId)
                .distinct()
                .toList();
        Map<Integer, Integer> doctorToTotalPatients = visitRepository.countPatientsPerDoctor(doctorsIds).stream()
                .collect(Collectors.toMap(DoctorPatientsCount::doctorId, d -> d.totalPatients().intValue()));

        Map<Integer, List<VisitDto>> patientsToVisits = patientsLastVisits.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream()
                                .map(visit -> buildVisitDto(visit, doctorToTotalPatients))
                                .toList()));

        List<PatientDto> patients = page.getContent().stream()
                .map(patient -> PatientDto.builder()
                        .firstName(patient.getFirstName())
                        .lastName(patient.getLastName())
                        .lastVisits(patientsToVisits.getOrDefault(patient.getId(), List.of()))
                        .build()
                ).toList();

        return new PatientListResponse(patients, (int) page.getTotalElements());
    }

    private VisitDto buildVisitDto(Visit visit,
                                   Map<Integer, Integer> doctorToTotalPatients) {
        Doctor doctor = visit.getDoctor();
        var doctorDto = DoctorDto.builder()
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .totalPatients(doctorToTotalPatients.getOrDefault(doctor.getId(), 0))
                .build();

        ZoneId zone = ZoneId.of(doctor.getTimezone());
        return VisitDto.builder()
                .start(ApiTimeFormat.parse(visit.getStartDateTime(), zone))
                .end(ApiTimeFormat.parse(visit.getEndDateTime(), zone))
                .doctor(doctorDto)
                .build();
    }
}
