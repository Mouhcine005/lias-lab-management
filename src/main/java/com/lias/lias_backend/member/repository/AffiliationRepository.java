package com.lias.lias_backend.member.repository;

import com.lias.lias_backend.member.entity.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
    List<Affiliation> findByMemberId(Long memberId);
    Optional<Affiliation> findByMemberIdAndEndDateIsNull(Long memberId);
    @Query("SELECT a FROM Affiliation a WHERE a.member.id = :memberId AND a.endDate IS NULL")
    Optional<Affiliation> findActiveByMemberId(@Param("memberId") Long memberId);
}