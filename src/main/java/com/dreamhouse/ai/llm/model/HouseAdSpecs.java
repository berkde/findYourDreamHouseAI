package com.dreamhouse.ai.llm.model;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class HouseAdSpecs {
    public static Specification<HouseAdEntity> byFilter(FilterSpec f) {
        return (root, q, cb) -> {
            var p = new ArrayList<Predicate>();
            if (f.city != null) p.add(cb.equal(cb.lower(root.get("city")), f.city.toLowerCase()));
            if (f.neighborhoods != null && !f.neighborhoods.isEmpty()) p.add(root.get("neighborhood").in(f.neighborhoods));
            if (f.types != null && !f.types.isEmpty()) p.add(root.get("type").in(f.types));
            if (f.minBeds != null) p.add(cb.ge(root.get("beds"), f.minBeds));
            if (f.maxBeds != null) p.add(cb.le(root.get("beds"), f.maxBeds));
            if (f.maxPrice != null) p.add(cb.le(root.get("price"), f.maxPrice));
            if (f.hasParking != null) p.add(cb.equal(root.get("parking"), f.hasParking));
            if (f.petsAllowed != null) p.add(cb.equal(root.get("petsAllowed"), f.petsAllowed));
            if (f.keywords != null && !f.keywords.isBlank()) {
                String like = "%" + f.keywords.toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
