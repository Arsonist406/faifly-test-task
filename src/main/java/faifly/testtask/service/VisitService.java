package faifly.testtask.service;

import faifly.testtask.dto.ApiTimeFormat;
import faifly.testtask.dto.visit.CreateVisitRequest;
import faifly.testtask.dto.visit.VisitResponse;
import faifly.testtask.entity.Doctor;
import faifly.testtask.entity.Patient;
import faifly.testtask.entity.Visit;
import faifly.testtask.exception.ResourceNotFoundException;
import faifly.testtask.exception.VisitOverlapException;
import faifly.testtask.repository.DoctorRepository;
import faifly.testtask.repository.PatientRepository;
import faifly.testtask.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Transactional
    public VisitResponse create(CreateVisitRequest request) {
        Doctor doctor = doctorRepository.findByIdForUpdate(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor %d does not exist".formatted(request.doctorId())));

        if (!patientRepository.existsById(request.patientId())) {
            throw new ResourceNotFoundException("Patient %d does not exist".formatted(request.patientId()));
        }

        ZoneId zoneId = parseTimezone(doctor);
        Instant start = request.start().atZone(zoneId).toInstant();
        Instant end = request.end().atZone(zoneId).toInstant();

        if (visitRepository.existsByDoctorIdAndStartDateTimeBeforeAndEndDateTimeAfter(doctor.getId(), end, start)) {
            throw new VisitOverlapException("Doctor %d already has a visit overlapping %s - %s (%s)"
                    .formatted(doctor.getId(), request.start(), request.end(), zoneId));
        }

        Patient patient = patientRepository.getReferenceById(request.patientId());
        var visit = Visit.builder()
                .patient(patient)
                .doctor(doctor)
                .startDateTime(start)
                .endDateTime(end)
                .build();

        Visit newVisit = visitRepository.save(visit);
        log.debug("New visit created: {}", newVisit.getId());

        return VisitResponse.builder()
                .id(visit.getId())
                .start(ApiTimeFormat.parse(visit.getStartDateTime(), zoneId))
                .end(ApiTimeFormat.parse(visit.getEndDateTime(), zoneId))
                .patientId(visit.getPatient().getId())
                .doctorId(visit.getDoctor().getId())
                .build();
    }

    private ZoneId parseTimezone(Doctor doctor) {
        try {
            return ZoneId.of(doctor.getTimezone());
        } catch (DateTimeException e) {
            log.warn("Could not parse doctor {} time zone {}", doctor.getId(), doctor.getTimezone(), e);
            throw new RuntimeException("Doctor %d has an invalid timezone '%s'"
                    .formatted(doctor.getId(), doctor.getTimezone()), e);
        }
    }
}
