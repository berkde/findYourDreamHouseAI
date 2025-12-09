package com.dreamhouse.ai.llm.model.auxilary;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterSpec {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> city;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> state;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> neighborhoods;

    private Double minPrice, maxPrice;
    private Integer minBeds, maxBeds, minBaths, minSqft, maxSqft, minYearBuilt;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> types;

    private Boolean hasParking, petsAllowed, waterfront;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<double[]> polygon;

    public FilterSpec() {}

    public FilterSpec(List<String> city, List<String> state, List<String> neighborhoods,
                      Double minPrice, Double maxPrice,
                      Integer minBeds, Integer maxBeds, Integer minBaths,
                      Integer minSqft, Integer maxSqft, Integer minYearBuilt,
                      List<String> types, Boolean hasParking, Boolean petsAllowed,
                      Boolean waterfront, List<double[]> polygon) {
        this.city = city;
        this.state = state;
        this.neighborhoods = neighborhoods;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minBeds = minBeds;
        this.maxBeds = maxBeds;
        this.minBaths = minBaths;
        this.minSqft = minSqft;
        this.maxSqft = maxSqft;
        this.minYearBuilt = minYearBuilt;
        this.types = types;
        this.hasParking = hasParking;
        this.petsAllowed = petsAllowed;
        this.waterfront = waterfront;
        this.polygon = polygon;
    }

    // ===== LIST FIELDS =====
    // Return null when empty -> your Specification can keep using `if (getX() != null)` safely.

    public List<String> getCity() {
        return (city == null || city.isEmpty()) ? null : city;
    }
    public void setCity(List<String> city) { this.city = city; }

    public List<String> getState() {
        return (state == null || state.isEmpty()) ? null : state;
    }
    public void setState(List<String> state) { this.state = state; }

    public List<String> getNeighborhoods() {
        return (neighborhoods == null || neighborhoods.isEmpty()) ? null : neighborhoods;
    }
    public void setNeighborhoods(List<String> neighborhoods) { this.neighborhoods = neighborhoods; }

    public List<String> getTypes() {
        return (types == null || types.isEmpty()) ? null : types;
    }
    public void setTypes(List<String> types) { this.types = types; }


    public List<double[]> getPolygon() {
        return (polygon == null || polygon.isEmpty()) ? null : polygon;
    }
    public void setPolygon(List<double[]> polygon) { this.polygon = polygon; }

    // ===== NUMERIC FIELDS =====
    // Treat 0 or negative as "no constraint" (return null).

    public Double getMinPrice() {
        return (minPrice != null && minPrice > 0) ? minPrice : null;
    }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() {
        return (maxPrice != null && maxPrice > 0) ? maxPrice : null;
    }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public Integer getMinBeds() {
        return (minBeds != null && minBeds > 0) ? minBeds : null;
    }
    public void setMinBeds(Integer minBeds) { this.minBeds = minBeds; }

    public Integer getMaxBeds() {
        return (maxBeds != null && maxBeds > 0) ? maxBeds : null;
    }
    public void setMaxBeds(Integer maxBeds) { this.maxBeds = maxBeds; }

    public Integer getMinBaths() {
        return (minBaths != null && minBaths > 0) ? minBaths : null;
    }
    public void setMinBaths(Integer minBaths) { this.minBaths = minBaths; }

    public Integer getMinSqft() {
        return (minSqft != null && minSqft > 0) ? minSqft : null;
    }
    public void setMinSqft(Integer minSqft) { this.minSqft = minSqft; }

    public Integer getMaxSqft() {
        return (maxSqft != null && maxSqft > 0) ? maxSqft : null;
    }
    public void setMaxSqft(Integer maxSqft) { this.maxSqft = maxSqft; }

    public Integer getMinYearBuilt() {
        return (minYearBuilt != null && minYearBuilt > 0) ? minYearBuilt : null;
    }
    public void setMinYearBuilt(Integer minYearBuilt) { this.minYearBuilt = minYearBuilt; }

    public Boolean getHasParking() { return hasParking; }
    public void setHasParking(Boolean hasParking) { this.hasParking = hasParking; }

    public Boolean getPetsAllowed() { return petsAllowed; }
    public void setPetsAllowed(Boolean petsAllowed) { this.petsAllowed = petsAllowed; }

    public Boolean getWaterfront() { return waterfront; }
    public void setWaterfront(Boolean waterfront) { this.waterfront = waterfront; }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FilterSpec that)) return false;
        return Objects.equals(city, that.city)
                && Objects.equals(state, that.state)
                && Objects.equals(neighborhoods, that.neighborhoods)
                && Objects.equals(minPrice, that.minPrice)
                && Objects.equals(maxPrice, that.maxPrice)
                && Objects.equals(minBeds, that.minBeds)
                && Objects.equals(maxBeds, that.maxBeds)
                && Objects.equals(minBaths, that.minBaths)
                && Objects.equals(minSqft, that.minSqft)
                && Objects.equals(maxSqft, that.maxSqft)
                && Objects.equals(minYearBuilt, that.minYearBuilt)
                && Objects.equals(types, that.types)
                && Objects.equals(hasParking, that.hasParking)
                && Objects.equals(petsAllowed, that.petsAllowed)
                && Objects.equals(waterfront, that.waterfront)
                && Objects.equals(polygon, that.polygon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, state, neighborhoods, minPrice, maxPrice,
                minBeds, maxBeds, minBaths, minSqft, maxSqft, minYearBuilt,
                types, hasParking, petsAllowed, waterfront, polygon);
    }

    @Override
    public String toString() {
        return "FilterSpec{" +
                "city=" + city +
                ", state=" + state +
                ", neighborhoods=" + neighborhoods +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", minBeds=" + minBeds +
                ", maxBeds=" + maxBeds +
                ", minBaths=" + minBaths +
                ", minSqft=" + minSqft +
                ", maxSqft=" + maxSqft +
                ", minYearBuilt=" + minYearBuilt +
                ", types=" + types +
                ", hasParking=" + hasParking +
                ", petsAllowed=" + petsAllowed +
                ", waterfront=" + waterfront +
                ", polygon=" + polygon +
                '}';
    }
}