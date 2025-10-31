package com.ehr.springcore.repository;

import com.ehr.springcore.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {
    
    List<Metric> findByMetricName(String metricName);
    
    List<Metric> findByTimestampBetween(Instant start, Instant end);
}
