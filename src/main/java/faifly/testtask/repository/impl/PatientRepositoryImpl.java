package faifly.testtask.repository.impl;

import faifly.testtask.repository.PatientPageQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PatientRepositoryImpl implements PatientPageQuery {

    private static final char ESCAPE = '\\';

    private static final String QUERY = """
            SELECT new faifly.testtask.repository.PatientPageQuery$PatientVisitRow(
                    t.total,
                    f.id,
                    f.firstName,
                    f.lastName,
                    d.id,
                    d.firstName,
                    d.lastName,
                    d.timezone,
                    v.startDateTime,
                    v.endDateTime,
                    (SELECT count(DISTINCT dv.patient.id) FROM Visit dv WHERE dv.doctor.id = d.id))
            FROM (SELECT count(p.id) AS total FROM Patient p%1$s) t
            LEFT JOIN (SELECT p.id AS id, p.firstName AS firstName, p.lastName AS lastName
                       FROM Patient p%1$s
                       ORDER BY p.lastName, p.firstName, p.id
                       LIMIT :size OFFSET :offset) f ON 1 = 1
            LEFT JOIN Visit v ON v.patient.id = f.id%2$s
                  AND v.startDateTime = (SELECT max(v2.startDateTime)
                                         FROM Visit v2
                                         WHERE v2.patient.id = v.patient.id AND v2.doctor.id = v.doctor.id)
            LEFT JOIN Doctor d ON d.id = v.doctor.id
            ORDER BY f.lastName, f.firstName, f.id, v.startDateTime DESC
            """;

    private final EntityManager entityManager;

    @Override
    public List<PatientVisitRow> findPageWithLastVisits(String search,
                                                        Collection<Integer> doctorIds,
                                                        int page,
                                                        int size) {
        String[] tokens = search == null ? new String[0] : search.split("\\s+");
        List<String> conditions = new ArrayList<>();

        if (tokens.length == 1) {
            conditions.add("(p.firstName LIKE :token1 ESCAPE :escape OR p.lastName LIKE :token1 ESCAPE :escape)");
        } else if (tokens.length > 1) {
            conditions.add("""
                    ((p.firstName LIKE :token1 ESCAPE :escape AND p.lastName LIKE :token2 ESCAPE :escape)
                     OR (p.firstName LIKE :token2 ESCAPE :escape AND p.lastName LIKE :token1 ESCAPE :escape))
                    """);
        }
        if (doctorIds != null) {
            conditions.add("EXISTS (SELECT 1 FROM Visit fv WHERE fv.patient.id = p.id AND fv.doctor.id IN :doctorIds)");
        }

        String jpql = QUERY.formatted(
                conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions),
                doctorIds == null ? "" : " AND v.doctor.id IN :doctorIds");

        TypedQuery<PatientVisitRow> query = entityManager.createQuery(jpql, PatientVisitRow.class)
                .setParameter("size", size)
                .setParameter("offset", (long) page * size);

        if (tokens.length > 0) {
            query.setParameter("escape", ESCAPE).setParameter("token1", likePattern(tokens[0]));
        }
        if (tokens.length > 1) {
            query.setParameter("token2", likePattern(tokens[1]));
        }
        if (doctorIds != null) {
            query.setParameter("doctorIds", doctorIds);
        }

        return query.getResultList();
    }

    private String likePattern(String token) {
        StringBuilder pattern = new StringBuilder();
        token.chars().forEach(character -> {
            if (character == ESCAPE || character == '%' || character == '_') {
                pattern.append(ESCAPE);
            }
            pattern.appendCodePoint(character);
        });
        return pattern.append('%').toString();
    }
}
