package com.lias.lias_backend.history.repository;

import com.lias.lias_backend.history.entity.MemberStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, Long> {
    List<MemberStatusHistory> findByMemberIdOrderByStartDateDesc(Long memberId);
    Optional<MemberStatusHistory> findByMemberIdAndEndDateIsNull(Long memberId);
}
