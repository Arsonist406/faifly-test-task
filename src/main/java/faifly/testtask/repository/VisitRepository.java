package faifly.testtask.repository;

import faifly.testtask.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByDoctorIdAndStartDateTimeBeforeAndEndDateTimeAfter(Integer doctorId, Instant end, Instant start);

    @Query("""
            SELECT v FROM Visit v
            JOIN FETCH v.doctor
            WHERE v.patient.id IN :patientIds
              AND v.startDateTime = (
                  SELECT max(v2.startDateTime) FROM Visit v2
                  WHERE v2.patient.id = v.patient.id AND v2.doctor.id = v.doctor.id)
            ORDER BY v.patient.id, v.startDateTime DESC
            """)
    List<Visit> findLastVisitToEachDoctorByPatients(@Param("patientIds") Collection<Integer> patientIds);

    @Query("""
            SELECT v FROM Visit v
            JOIN FETCH v.doctor
            WHERE v.patient.id IN :patientIds
              AND v.doctor.id IN :doctorIds
              AND v.startDateTime = (
                  SELECT max(v2.startDateTime) FROM Visit v2
                  WHERE v2.patient.id = v.patient.id AND v2.doctor.id = v.doctor.id)
            ORDER BY v.patient.id, v.startDateTime DESC
            """)
    List<Visit> findLastVisitByPatientsAndDoctors(@Param("patientIds") Collection<Integer> patientIds,
                                                  @Param("doctorIds") Collection<Integer> doctorIds);

    @Query("""
            SELECT new faifly.testtask.repository.VisitRepository$DoctorPatientsCount(v.doctor.id, count(distinct v.patient.id))
            FROM Visit v
            WHERE v.doctor.id IN :doctorIds
            GROUP BY v.doctor.id
            """)
    List<DoctorPatientsCount> countPatientsPerDoctor(@Param("doctorIds") Collection<Integer> doctorIds);

    record DoctorPatientsCount(
            Integer doctorId,
            Long totalPatients
    ) {
    }
}
