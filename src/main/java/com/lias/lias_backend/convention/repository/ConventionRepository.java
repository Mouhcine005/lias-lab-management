package com.lias.lias_backend.convention.repository;

import com.lias.lias_backend.convention.entity.Convention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConventionRepository extends JpaRepository<Convention, Long> {
    List<Convention> findByStatus(Convention.ConventionStatus status);
    List<Convention> findByPartnerNameContainingIgnoreCase(String partnerName);
}
