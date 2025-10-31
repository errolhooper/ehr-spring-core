package com.ehr.springcore.repository;

import com.ehr.springcore.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByEventName(String eventName);
    
    List<Event> findByTimestampBetween(Instant start, Instant end);
}
