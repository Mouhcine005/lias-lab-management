package com.lias.lias_backend.history.repository;

import com.lias.lias_backend.history.entity.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleHistoryRepository extends JpaRepository<RoleHistory, Long> {
    List<RoleHistory> findByUserIdOrderByStartDateDesc(Long userId);
    Optional<RoleHistory> findByUserIdAndEndDateIsNull(Long userId);
}
