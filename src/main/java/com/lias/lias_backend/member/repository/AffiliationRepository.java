package com.lias.lias_backend.member.repository;

import com.lias.lias_backend.member.entity.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AffiliationRepository extends JpaRepository<Affiliation, Long> {
    List<Affiliation> findByMemberId(Long memberId);
    Optional<Affiliation> findByMemberIdAndEndDateIsNull(Long memberId);
}