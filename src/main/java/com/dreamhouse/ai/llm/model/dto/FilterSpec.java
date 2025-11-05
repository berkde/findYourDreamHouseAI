package com.dreamhouse.ai.llm.model.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FilterSpec {
    public String city;
    public String state;
    public List<String> neighborhoods;
    public Double minPrice, maxPrice;
    public Integer minBeds, maxBeds, minBaths, minSqft, maxSqft, minYearBuilt;
    public List<String> types;
    public Boolean hasParking, petsAllowed, waterfront;
    public String keywords;
    public List<double[]> polygon;

    public FilterSpec() {
        /*
            This constructor has been intentionally left empty for
            object marshalling and serialization motives
        */
    }
    public FilterSpec(String city, String state, List<String> neighborhoods, Double minPrice, Double maxPrice, Boolean hasParking, Integer minBeds, Integer maxBeds, Integer minBaths, Integer minSqft, Integer maxSqft, Integer minYearBuilt, List<String> types, Boolean parking, Boolean petsAllowed, Boolean waterfront, String keywords, List<double[]> polygon) {
        this.city = city;
        this.state = state;
        this.neighborhoods = neighborhoods;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.hasParking = hasParking;
        this.minBeds = minBeds;
        this.maxBeds = maxBeds;
        this.minBaths = minBaths;
        this.minSqft = minSqft;
        this.maxSqft = maxSqft;
        this.minYearBuilt = minYearBuilt;
        this.types = types;
        this.hasParking = parking;
        this.petsAllowed = petsAllowed;
        this.waterfront = waterfront;
        this.keywords = keywords;
        this.polygon = polygon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getNeighborhoods() {
        if (neighborhoods == null) return Collections.emptyList();
        return List.copyOf(neighborhoods);
    }

    public void setNeighborhoods(List<String> neighborhoods) {
        this.neighborhoods = neighborhoods;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Integer getMinBeds() {
        return minBeds;
    }

    public void setMinBeds(Integer minBeds) {
        this.minBeds = minBeds;
    }

    public Integer getMinBaths() {
        return minBaths;
    }

    public void setMinBaths(Integer minBaths) {
        this.minBaths = minBaths;
    }

    public Integer getMinSqft() {
        return minSqft;
    }

    public void setMinSqft(Integer minSqft) {
        this.minSqft = minSqft;
    }

    public Integer getMaxSqft() {
        return maxSqft;
    }

    public void setMaxSqft(Integer maxSqft) {
        this.maxSqft = maxSqft;
    }

    public Integer getMinYearBuilt() {
        return minYearBuilt;
    }

    public void setMinYearBuilt(Integer minYearBuilt) {
        this.minYearBuilt = minYearBuilt;
    }

    public List<String> getTypes() {
        if (types == null) return Collections.emptyList();
        return List.copyOf(types);
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Boolean getHasParking() {
        return hasParking;
    }

    public void setHasParking(Boolean hasParking) {
        this.hasParking = hasParking;
    }

    public Boolean getPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(Boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public Boolean getWaterfront() {
        return waterfront;
    }

    public void setWaterfront(Boolean waterfront) {
        this.waterfront = waterfront;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<double[]> getPolygon() {
        if (polygon == null) return Collections.emptyList();
        return List.copyOf(polygon);
    }

    public void setPolygon(List<double[]> polygon) {this.polygon = polygon;}

    public Integer getMaxBeds() {
        return maxBeds;
    }

    public void setMaxBeds(Integer maxBeds) {
        this.maxBeds = maxBeds;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FilterSpec that)) return false;
        return Objects.equals(city, that.city) && Objects.equals(state, that.state) && Objects.equals(neighborhoods, that.neighborhoods) && Objects.equals(minPrice, that.minPrice) && Objects.equals(maxPrice, that.maxPrice) && Objects.equals(minBeds, that.minBeds) && Objects.equals(minBaths, that.minBaths) && Objects.equals(minSqft, that.minSqft) && Objects.equals(maxSqft, that.maxSqft) && Objects.equals(minYearBuilt, that.minYearBuilt) && Objects.equals(types, that.types) && Objects.equals(hasParking, that.hasParking) && Objects.equals(petsAllowed, that.petsAllowed) && Objects.equals(waterfront, that.waterfront) && Objects.equals(keywords, that.keywords) && Objects.equals(polygon, that.polygon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, state, neighborhoods, minPrice, maxPrice, minBeds, minBaths, minSqft, maxSqft, minYearBuilt, types, hasParking, petsAllowed, waterfront, keywords, polygon);
    }
}
