package faifly.testtask.repository.spec;

import faifly.testtask.entity.Patient;
import faifly.testtask.entity.Visit;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PatientSpecifications {

    public static final char ESCAPE = '\\';

    private PatientSpecifications() {
    }

    public static Specification<Patient> matching(String search,
                                                  Collection<Integer> doctorIds) {
        List<Specification<Patient>> specifications = new ArrayList<>();

        if (StringUtils.hasText(search)) {
            specifications.add(nameMatches(search.trim().split("\\s+")));
        }
        if (!CollectionUtils.isEmpty(doctorIds)) {
            specifications.add(visitedAnyOf(doctorIds));
        }

        return specifications.isEmpty()
                ? Specification.unrestricted()
                : Specification.allOf(specifications);
    }

    private static Specification<Patient> nameMatches(String[] tokens) {
        if (tokens.length == 1) {
            String pattern = likePattern(tokens[0]);
            return (root, query, builder) -> builder.or(
                    builder.like(root.get("firstName"), pattern, ESCAPE),
                    builder.like(root.get("lastName"), pattern, ESCAPE));
        }

        String first = likePattern(tokens[0]);
        String second = likePattern(tokens[1]);
        return (root, query, builder) -> builder.or(
                builder.and(
                        builder.like(root.get("firstName"), first, ESCAPE),
                        builder.like(root.get("lastName"), second, ESCAPE)),
                builder.and(
                        builder.like(root.get("firstName"), second, ESCAPE),
                        builder.like(root.get("lastName"), first, ESCAPE)));
    }

    private static String likePattern(String token) {
        StringBuilder pattern = new StringBuilder();
        token.chars().forEach(character -> {
            if (character == ESCAPE || character == '%' || character == '_') {
                pattern.append(ESCAPE);
            }
            pattern.appendCodePoint(character);
        });
        return pattern.append('%').toString();
    }

    private static Specification<Patient> visitedAnyOf(Collection<Integer> doctorIds) {
        return (root, query, builder) -> {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Visit> visit = subquery.from(Visit.class);
            subquery.select(builder.literal(1))
                    .where(builder.equal(visit.get("patient"), root),
                            visit.get("doctor").get("id").in(doctorIds));
            return builder.exists(subquery);
        };
    }
}
