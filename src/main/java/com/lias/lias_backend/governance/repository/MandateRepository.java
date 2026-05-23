package com.lias.lias_backend.governance.repository;

import com.lias.lias_backend.governance.entity.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MandateRepository extends JpaRepository<Mandate, Long> {
    List<Mandate> findByMemberId(Long memberId);
    List<Mandate> findByRole(Mandate.MandateRole role);

    // Find current active mandate by role (no end date or end date in future)
    @Query("SELECT m FROM Mandate m WHERE m.role = :role AND (m.endDate IS NULL OR m.endDate > CURRENT_DATE)")
    Optional<Mandate> findActiveByRole(Mandate.MandateRole role);

    // Find all active mandates
    @Query("SELECT m FROM Mandate m WHERE m.endDate IS NULL OR m.endDate > CURRENT_DATE")
    List<Mandate> findAllActive();

    // Find active mandate for a specific member
    @Query("SELECT m FROM Mandate m WHERE m.member.id = :memberId AND (m.endDate IS NULL OR m.endDate > CURRENT_DATE)")
    List<Mandate> findActiveByMemberId(Long memberId);
}