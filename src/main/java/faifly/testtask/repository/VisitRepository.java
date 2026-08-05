package faifly.testtask.repository;

import faifly.testtask.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByDoctorIdAndStartDateTimeBeforeAndEndDateTimeAfter(Integer doctorId, Instant end, Instant start);
}
