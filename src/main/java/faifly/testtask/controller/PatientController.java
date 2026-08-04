package faifly.testtask.controller;

import faifly.testtask.dto.patient.PatientListQuery;
import faifly.testtask.dto.patient.PatientListResponse;
import faifly.testtask.service.PatientQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientQueryService patientQueryService;

    @GetMapping
    public PatientListResponse getPatients(@Valid PatientListQuery query) {
        return patientQueryService.getPatients(query);
    }
}
