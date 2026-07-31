package com.airproject.airproject.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response wrapper for historical air quality data with aggregation metadata.
 */
public class HistoricalDataResponse {

    private Range range;
    private String aggregationInterval;
    private List<AggregatedReadingDto> data;

    public HistoricalDataResponse() {
    }

    public HistoricalDataResponse(Range range, String aggregationInterval, List<AggregatedReadingDto> data) {
        this.range = range;
        this.aggregationInterval = aggregationInterval;
        this.data = data;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public String getAggregationInterval() {
        return aggregationInterval;
    }

    public void setAggregationInterval(String aggregationInterval) {
        this.aggregationInterval = aggregationInterval;
    }

    public List<AggregatedReadingDto> getData() {
        return data;
    }

    public void setData(List<AggregatedReadingDto> data) {
        this.data = data;
    }

    /**
     * Represents the resolved time range of the query.
     */
    public static class Range {
        private Instant from;
        private Instant to;

        public Range() {
        }

        public Range(Instant from, Instant to) {
            this.from = from;
            this.to = to;
        }

        public Instant getFrom() {
            return from;
        }

        public void setFrom(Instant from) {
            this.from = from;
        }

        public Instant getTo() {
            return to;
        }

        public void setTo(Instant to) {
            this.to = to;
        }
    }

    /**
     * A single aggregated data point (serializable DTO mirroring the AggregatedReading projection).
     */
    public static class AggregatedReadingDto {
        private Instant bucket;
        private String deviceId;
        private Double avgTemperature;
        private Double avgHumidity;
        private Double avgCo2;
        private Double avgPm1_0;
        private Double avgPm2_5;
        private Double avgPm10;

        public AggregatedReadingDto() {
        }

        public AggregatedReadingDto(Instant bucket, String deviceId, Double avgTemperature, Double avgHumidity,
                                     Double avgCo2, Double avgPm1_0, Double avgPm2_5, Double avgPm10) {
            this.bucket = bucket;
            this.deviceId = deviceId;
            this.avgTemperature = avgTemperature;
            this.avgHumidity = avgHumidity;
            this.avgCo2 = avgCo2;
            this.avgPm1_0 = avgPm1_0;
            this.avgPm2_5 = avgPm2_5;
            this.avgPm10 = avgPm10;
        }

        /**
         * Creates a DTO from the Spring Data projection interface.
         */
        public static AggregatedReadingDto fromProjection(AggregatedReading projection) {
            return new AggregatedReadingDto(
                    projection.getBucket(),
                    projection.getDeviceId(),
                    projection.getAvgTemperature(),
                    projection.getAvgHumidity(),
                    projection.getAvgCo2(),
                    projection.getAvgPm1_0(),
                    projection.getAvgPm2_5(),
                    projection.getAvgPm10()
            );
        }

        public Instant getBucket() {
            return bucket;
        }

        public void setBucket(Instant bucket) {
            this.bucket = bucket;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public Double getAvgTemperature() {
            return avgTemperature;
        }

        public void setAvgTemperature(Double avgTemperature) {
            this.avgTemperature = avgTemperature;
        }

        public Double getAvgHumidity() {
            return avgHumidity;
        }

        public void setAvgHumidity(Double avgHumidity) {
            this.avgHumidity = avgHumidity;
        }

        public Double getAvgCo2() {
            return avgCo2;
        }

        public void setAvgCo2(Double avgCo2) {
            this.avgCo2 = avgCo2;
        }

        public Double getAvgPm1_0() {
            return avgPm1_0;
        }

        public void setAvgPm1_0(Double avgPm1_0) {
            this.avgPm1_0 = avgPm1_0;
        }

        public Double getAvgPm2_5() {
            return avgPm2_5;
        }

        public void setAvgPm2_5(Double avgPm2_5) {
            this.avgPm2_5 = avgPm2_5;
        }

        public Double getAvgPm10() {
            return avgPm10;
        }

        public void setAvgPm10(Double avgPm10) {
            this.avgPm10 = avgPm10;
        }
    }
}
