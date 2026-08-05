package faifly.testtask.repository;

import faifly.testtask.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Integer>, PatientPageQuery {
}
