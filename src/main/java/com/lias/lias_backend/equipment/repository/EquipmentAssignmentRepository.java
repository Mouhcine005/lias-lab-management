package com.lias.lias_backend.equipment.repository;

import com.lias.lias_backend.equipment.entity.EquipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentAssignmentRepository extends JpaRepository<EquipmentAssignment, Long> {

    List<EquipmentAssignment> findByEquipmentIdAndReturnDateIsNull(Long equipmentId);

    List<EquipmentAssignment> findByMemberIdAndReturnDateIsNull(Long memberId);

    List<EquipmentAssignment> findByMemberId(Long memberId);

    List<EquipmentAssignment> findByEquipmentId(Long equipmentId);

    @Query("SELECT ea FROM EquipmentAssignment ea WHERE ea.returnDate IS NULL ORDER BY ea.assignmentDate DESC")
    List<EquipmentAssignment> findAllActive();

    @Query("SELECT DISTINCT ea.member.id FROM EquipmentAssignment ea WHERE ea.returnDate IS NULL")
    List<Long> findMemberIdsWithActiveAssignments();

    boolean existsByFromRequestId(Long requestId);

    @Query("SELECT SUM(ea.quantityAssigned) FROM EquipmentAssignment ea " +
            "WHERE ea.equipment.id = :equipmentId AND ea.returnDate IS NULL")
    Integer sumActiveQuantityByEquipment(Long equipmentId);

    List<EquipmentAssignment> findByAssignmentDateBetween(LocalDate from, LocalDate to);
}