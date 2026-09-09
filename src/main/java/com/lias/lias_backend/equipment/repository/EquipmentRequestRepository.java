package com.lias.lias_backend.equipment.repository;

import com.lias.lias_backend.equipment.entity.EquipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {

    List<EquipmentRequest> findByRequestedByIdOrderByRequestDateDesc(Long memberId);

    List<EquipmentRequest> findByStatusOrderByRequestDateDesc(EquipmentRequest.RequestStatus status);

    boolean existsByRequestedByIdAndEquipmentNameIgnoreCaseAndStatus(
            Long memberId, String equipmentName, EquipmentRequest.RequestStatus status);
}