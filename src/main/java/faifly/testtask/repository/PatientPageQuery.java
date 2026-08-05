package faifly.testtask.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface PatientPageQuery {

    List<PatientVisitRow> findPageWithLastVisits(String search,
                                                 Collection<Integer> doctorIds,
                                                 int page,
                                                 int size);

    record PatientVisitRow(
            Long total,
            Integer patientId,
            String firstName,
            String lastName,
            Integer doctorId,
            String doctorFirstName,
            String doctorLastName,
            String doctorTimezone,
            Instant start,
            Instant end,
            Long doctorTotalPatients
    ) {
    }
}
