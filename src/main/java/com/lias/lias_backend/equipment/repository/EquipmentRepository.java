package com.lias.lias_backend.equipment.repository;

import com.lias.lias_backend.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findBySerialNumber(String serialNumber);

    List<Equipment> findByStatus(Equipment.EquipmentStatus status);

    List<Equipment> findByAssignedToId(Long memberId);

    @Query("""
            SELECT DISTINCT m.id FROM Member m
            WHERE m.id NOT IN (
                SELECT DISTINCT ea.member.id FROM EquipmentAssignment ea
            )
            """)
    List<Long> findMemberIdsWithNoEquipmentEver();

    @Query("SELECT e FROM Equipment e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Equipment> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT e FROM Equipment e WHERE e.availableQuantity > 0")
    List<Equipment> findAvailable();

    List<Equipment> findByArrivalDateBetween(LocalDate from, LocalDate to);
}