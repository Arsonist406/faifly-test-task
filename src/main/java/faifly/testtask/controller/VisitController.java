package faifly.testtask.controller;

import faifly.testtask.dto.visit.CreateVisitRequest;
import faifly.testtask.dto.visit.VisitResponse;
import faifly.testtask.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    public VisitResponse create(@Valid @RequestBody CreateVisitRequest request) {
        return visitService.create(request);
    }
}
