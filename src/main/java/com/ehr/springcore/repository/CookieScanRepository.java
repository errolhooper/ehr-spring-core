package com.ehr.springcore.repository;

import com.ehr.springcore.entity.CookieScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CookieScanRepository extends JpaRepository<CookieScan, Long> {
}
