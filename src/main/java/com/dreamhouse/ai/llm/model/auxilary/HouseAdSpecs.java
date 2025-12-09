package com.dreamhouse.ai.llm.model.auxilary;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import jakarta.persistence.criteria.Predicate;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HouseAdSpecs {

    private static final Map<String, String> STATE_CANON = Map.ofEntries(
            Map.entry("new york", "ny"),
            Map.entry("ny", "ny"),

            Map.entry("michigan", "mi"),
            Map.entry("mi", "mi"),

            Map.entry("massachusetts", "ma"),
            Map.entry("ma", "ma"),

            Map.entry("rhode island", "ri"),
            Map.entry("ri", "ri"),

            Map.entry("washington", "wa"),
            Map.entry("wa", "wa"),

            Map.entry("new jersey", "nj"),
            Map.entry("nj", "nj")
    );

    private static String normalizeStateToken(String s) {
        if (s == null) return null;
        String key = s.trim().toLowerCase();
        return STATE_CANON.getOrDefault(key, key);
    }

    // ==========================
    // MAIN SPEC
    // ==========================
    public Specification<HouseAdEntity> byFilter(FilterSpec filter) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            // ---- CITY ----
            if (filter.getCity() != null && !filter.getCity().isEmpty()) {
                List<Predicate> cityPreds = new ArrayList<>();
                for (String c : filter.getCity()) {
                    if (c == null || c.isBlank()) continue;
                    String pattern = "%" + c.toLowerCase() + "%";
                    cityPreds.add(cb.like(cb.lower(root.get("city")), pattern));
                }
                if (!cityPreds.isEmpty()) {
                    predicates.add(cb.or(cityPreds.toArray(new Predicate[0])));
                }
            }

            // ---- STATE ----
            if (filter.getState() != null && !filter.getState().isEmpty()) {
                List<Predicate> statePreds = new ArrayList<>();
                for (String s : filter.getState()) {
                    if (s == null || s.isBlank()) continue;
                    String pattern = "%" + s.toLowerCase() + "%";
                    statePreds.add(cb.like(cb.lower(root.get("state")), pattern));
                }
                if (!statePreds.isEmpty()) {
                    predicates.add(cb.or(statePreds.toArray(new Predicate[0])));
                }
            }

            // ---- PRICE ----
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // ---- BEDS ----
            if (filter.getMinBeds() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("beds"), filter.getMinBeds()));
            }
            if (filter.getMaxBeds() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("beds"), filter.getMaxBeds()));
            }

            // ---- BATHS ----
            if (filter.getMinBaths() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("baths"), filter.getMinBaths()));
            }

            // ---- SQFT ----
            if (filter.getMinSqft() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sqft"), filter.getMinSqft()));
            }
            if (filter.getMaxSqft() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sqft"), filter.getMaxSqft()));
            }

            // ---- YEAR BUILT ----
            if (filter.getMinYearBuilt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearBuilt"), filter.getMinYearBuilt()));
            }

            // ---- TYPE (after normalization, no generic "house") ----
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                List<Predicate> typePreds = new ArrayList<>();
                for (String t : filter.getTypes()) {
                    if (t == null || t.isBlank()) continue;
                    String pattern = "%" + t.toLowerCase() + "%";
                    typePreds.add(cb.like(cb.lower(root.get("type")), pattern));
                }
                if (!typePreds.isEmpty()) {
                    predicates.add(cb.or(typePreds.toArray(new Predicate[0])));
                }
            }

            // ---- BOOLEANS (only when non-null) ----
            if (filter.getHasParking() != null) {
                predicates.add(cb.equal(root.get("parking"), filter.getHasParking()));
            }
            if (filter.getPetsAllowed() != null) {
                predicates.add(cb.equal(root.get("petsAllowed"), filter.getPetsAllowed()));
            }
            if (filter.getWaterfront() != null) {
                predicates.add(cb.equal(root.get("waterfront"), filter.getWaterfront()));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ==========================
    // NORMALIZATION
    // ==========================
    public FilterSpec normalizeFilter(@NotNull FilterSpec f) {

        // --- CITY: trim + lowercase ---
        if (f.getCity() != null) {
            f.setCity(
                    f.getCity().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .toList()
            );
        }

        // --- STATE: map to canonical code (ny, mi, ma, ri, wa, nj) ---
        if (f.getState() != null) {
            f.setState(
                    f.getState().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .map(HouseAdSpecs::normalizeStateToken)
                            .toList()
            );
        }

        // --- TYPES: plural → singular, lowercase, drop generic "house" ---
        if (f.getTypes() != null) {
            List<String> cleanedTypes = f.getTypes().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(t -> t.endsWith("s") ? t.substring(0, t.length() - 1) : t) // condos -> condo, apartments -> apartment, houses -> house
                    .filter(t -> !t.equals("house")) // *** KEY LINE *** ignore generic "house"
                    .toList();

            // if everything was "house"/"houses", treat as "no type filter"
            if (cleanedTypes.isEmpty()) {
                f.setTypes(null);
            } else {
                f.setTypes(cleanedTypes);
            }
        }

        // --- Treat false booleans as "no filter" (to avoid LLM default false) ---
        if (Boolean.FALSE.equals(f.getHasParking())) {
            f.setHasParking(null);
        }
        if (Boolean.FALSE.equals(f.getPetsAllowed())) {
            f.setPetsAllowed(null);
        }
        if (Boolean.FALSE.equals(f.getWaterfront())) {
            f.setWaterfront(null);
        }

        // Numeric 0 handling is already in FilterSpec getters, so we’re good there.

        return f;
    }
}
