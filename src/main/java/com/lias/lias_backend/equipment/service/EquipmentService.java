package com.lias.lias_backend.equipment.service;

import com.lias.lias_backend.equipment.dto.*;
import com.lias.lias_backend.equipment.entity.Equipment;
import com.lias.lias_backend.equipment.entity.EquipmentAssignment;
import com.lias.lias_backend.equipment.entity.EquipmentRequest;
import com.lias.lias_backend.equipment.repository.EquipmentAssignmentRepository;
import com.lias.lias_backend.equipment.repository.EquipmentRepository;
import com.lias.lias_backend.equipment.repository.EquipmentRequestRepository;
import com.lias.lias_backend.member.entity.Member;
import com.lias.lias_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentAssignmentRepository assignmentRepository;
    private final EquipmentRequestRepository requestRepository;
    private final MemberRepository memberRepository;

    // ── ARRIVALS (ADMIN) ──────────────────────────────────────

    public EquipmentResponse addEquipment(EquipmentArrivalRequest dto) {
        if (equipmentRepository.findBySerialNumber(dto.getSerialNumber()).isPresent()) {
            throw new RuntimeException("Equipment with serial number '" + dto.getSerialNumber() + "' already exists");
        }
        Equipment equipment = Equipment.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .serialNumber(dto.getSerialNumber())
                .quantity(dto.getQuantity())
                .availableQuantity(dto.getQuantity())
                .arrivalDate(dto.getArrivalDate())
                .condition(dto.getCondition())
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .notes(dto.getNotes())
                .build();
        return mapToEquipmentResponse(equipmentRepository.save(equipment));
    }

    public EquipmentResponse updateEquipment(Long id, EquipmentArrivalRequest dto) {
        Equipment equipment = findEquipmentOrThrow(id);
        if (!equipment.getSerialNumber().equals(dto.getSerialNumber())) {
            if (equipmentRepository.findBySerialNumber(dto.getSerialNumber()).isPresent()) {
                throw new RuntimeException("Serial number '" + dto.getSerialNumber() + "' already taken");
            }
        }
        equipment.setName(dto.getName());
        equipment.setDescription(dto.getDescription());
        equipment.setSerialNumber(dto.getSerialNumber());
        equipment.setCondition(dto.getCondition());
        equipment.setNotes(dto.getNotes());
        return mapToEquipmentResponse(equipmentRepository.save(equipment));
    }

    public void deleteEquipment(Long id) {
        Equipment equipment = findEquipmentOrThrow(id);
        List<EquipmentAssignment> active = assignmentRepository.findByEquipmentIdAndReturnDateIsNull(id);
        if (!active.isEmpty()) {
            throw new RuntimeException("Cannot delete equipment that is currently assigned. Return it first.");
        }
        equipmentRepository.delete(equipment);
    }

    // ── QUERIES (any authenticated) ───────────────────────────

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(this::mapToEquipmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        return mapToEquipmentResponse(findEquipmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAvailableEquipment() {
        return equipmentRepository.findAvailable().stream()
                .map(this::mapToEquipmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> searchEquipment(String keyword) {
        return equipmentRepository.searchByKeyword(keyword).stream()
                .map(this::mapToEquipmentResponse).collect(Collectors.toList());
    }

    // ── DISTRIBUTION (ADMIN) ──────────────────────────────────

    public AssignmentResponse assignEquipment(AssignmentRequest dto) {
        Equipment equipment = findEquipmentOrThrow(dto.getEquipmentId());
        Member member = findMemberOrThrow(dto.getMemberId());

        if (equipment.getAvailableQuantity() < dto.getQuantity()) {
            throw new RuntimeException("Not enough available quantity. Available: "
                    + equipment.getAvailableQuantity() + ", requested: " + dto.getQuantity());
        }

        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - dto.getQuantity());
        updateEquipmentStatus(equipment);
        equipment.setAssignedTo(member);
        equipment.setDistributionDate(LocalDate.now());
        equipmentRepository.save(equipment);

        EquipmentRequest fromRequest = null;
        if (dto.getFromRequestId() != null) {
            fromRequest = requestRepository.findById(dto.getFromRequestId())
                    .orElseThrow(() -> new RuntimeException("Request not found: " + dto.getFromRequestId()));
            fromRequest.setStatus(EquipmentRequest.RequestStatus.FULFILLED);
            fromRequest.setFulfilledByEquipment(equipment);
            requestRepository.save(fromRequest);
        }

        EquipmentAssignment assignment = EquipmentAssignment.builder()
                .equipment(equipment)
                .member(member)
                .quantityAssigned(dto.getQuantity())
                .assignmentDate(LocalDate.now())
                .assignmentNote(dto.getAssignmentNote())
                .fromRequest(fromRequest)
                .build();

        return mapToAssignmentResponse(assignmentRepository.save(assignment));
    }

    public AssignmentResponse returnEquipment(Long assignmentId, ReturnRequest dto) {
        EquipmentAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));
        if (assignment.getReturnDate() != null) {
            throw new RuntimeException("Already returned on " + assignment.getReturnDate());
        }
        assignment.setReturnDate(LocalDate.now());
        assignment.setReturnNote(dto.getReturnNote());

        Equipment equipment = assignment.getEquipment();
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + assignment.getQuantityAssigned());
        updateEquipmentStatus(equipment);
        equipmentRepository.save(equipment);

        return mapToAssignmentResponse(assignmentRepository.save(assignment));
    }

    // ── DISTRIBUTION OVERVIEW ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllActiveAssignments() {
        return assignmentRepository.findAllActive().stream()
                .map(this::mapToAssignmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByMember(Long memberId) {
        findMemberOrThrow(memberId);
        return assignmentRepository.findByMemberId(memberId).stream()
                .map(this::mapToAssignmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByEquipment(Long equipmentId) {
        return assignmentRepository.findByEquipmentId(equipmentId).stream()
                .map(this::mapToAssignmentResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getMemberIdsWithNoEquipment() {
        return equipmentRepository.findMemberIdsWithNoEquipmentEver();
    }

    // ── REQUESTS (any authenticated) ──────────────────────────

    public EquipmentRequestResponse submitRequest(EquipmentRequestDto dto) {
        Member requester = getCurrentMember();
        if (requestRepository.existsByRequestedByIdAndEquipmentNameIgnoreCaseAndStatus(
                requester.getId(), dto.getEquipmentName(), EquipmentRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("You already have a pending request for '" + dto.getEquipmentName() + "'");
        }
        EquipmentRequest request = EquipmentRequest.builder()
                .requestedBy(requester)
                .equipmentName(dto.getEquipmentName())
                .equipmentDescription(dto.getEquipmentDescription())
                .quantityRequested(dto.getQuantityRequested())
                .justification(dto.getJustification())
                .requestDate(LocalDate.now())
                .status(EquipmentRequest.RequestStatus.PENDING)
                .build();
        return mapToRequestResponse(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<EquipmentRequestResponse> getMyRequests() {
        return requestRepository
                .findByRequestedByIdOrderByRequestDateDesc(getCurrentMember().getId())
                .stream().map(this::mapToRequestResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentRequestResponse> getAllRequests(EquipmentRequest.RequestStatus status) {
        List<EquipmentRequest> requests = (status != null)
                ? requestRepository.findByStatusOrderByRequestDateDesc(status)
                : requestRepository.findAll();
        return requests.stream().map(this::mapToRequestResponse).collect(Collectors.toList());
    }

    // ── VALIDATION (DIRECTOR or ADMIN) ────────────────────────

    public EquipmentRequestResponse validateRequest(Long requestId, ValidationRequest dto) {
        if (dto.getDecision() == EquipmentRequest.RequestStatus.PENDING
                || dto.getDecision() == EquipmentRequest.RequestStatus.FULFILLED) {
            throw new RuntimeException("Decision must be APPROVED or REJECTED");
        }
        EquipmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
        if (request.getStatus() != EquipmentRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request already decided: " + request.getStatus());
        }
        request.setStatus(dto.getDecision());
        request.setValidatedBy(getCurrentMember());
        request.setValidationDate(LocalDate.now());
        request.setValidationNote(dto.getValidationNote());
        if (dto.getDecision() == EquipmentRequest.RequestStatus.APPROVED && dto.getEquipmentId() != null) {
            request.setFulfilledByEquipment(findEquipmentOrThrow(dto.getEquipmentId()));
        }
        return mapToRequestResponse(requestRepository.save(request));
    }

    // ── HELPERS ───────────────────────────────────────────────

    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found for: " + email));
    }

    private void updateEquipmentStatus(Equipment equipment) {
        if (equipment.getAvailableQuantity() == 0) {
            equipment.setStatus(Equipment.EquipmentStatus.ASSIGNED);
        } else if (equipment.getAvailableQuantity() < equipment.getQuantity()) {
            equipment.setStatus(Equipment.EquipmentStatus.PARTIALLY_ASSIGNED);
        } else {
            equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
        }
    }

    private Equipment findEquipmentOrThrow(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found: " + id));
    }

    private Member findMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));
    }

    private EquipmentResponse mapToEquipmentResponse(Equipment e) {
        return EquipmentResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .serialNumber(e.getSerialNumber())
                .quantity(e.getQuantity())
                .availableQuantity(e.getAvailableQuantity())
                .arrivalDate(e.getArrivalDate())
                .condition(e.getCondition())
                .status(e.getStatus())
                .notes(e.getNotes())
                .assignedToId(e.getAssignedTo() != null ? e.getAssignedTo().getId() : null)
                .assignedToName(e.getAssignedTo() != null
                        ? e.getAssignedTo().getFirstName() + " " + e.getAssignedTo().getLastName() : null)
                .distributionDate(e.getDistributionDate())
                .build();
    }

    private AssignmentResponse mapToAssignmentResponse(EquipmentAssignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .equipmentId(a.getEquipment().getId())
                .equipmentName(a.getEquipment().getName())
                .equipmentSerialNumber(a.getEquipment().getSerialNumber())
                .memberId(a.getMember().getId())
                .memberName(a.getMember().getFirstName() + " " + a.getMember().getLastName())
                .quantityAssigned(a.getQuantityAssigned())
                .assignmentDate(a.getAssignmentDate())
                .returnDate(a.getReturnDate())
                .assignmentNote(a.getAssignmentNote())
                .returnNote(a.getReturnNote())
                .fromRequestId(a.getFromRequest() != null ? a.getFromRequest().getId() : null)
                .build();
    }

    private EquipmentRequestResponse mapToRequestResponse(EquipmentRequest r) {
        return EquipmentRequestResponse.builder()
                .id(r.getId())
                .requestedById(r.getRequestedBy().getId())
                .requestedByName(r.getRequestedBy().getFirstName() + " " + r.getRequestedBy().getLastName())
                .equipmentName(r.getEquipmentName())
                .equipmentDescription(r.getEquipmentDescription())
                .quantityRequested(r.getQuantityRequested())
                .justification(r.getJustification())
                .requestDate(r.getRequestDate())
                .status(r.getStatus())
                .validatedById(r.getValidatedBy() != null ? r.getValidatedBy().getId() : null)
                .validatedByName(r.getValidatedBy() != null
                        ? r.getValidatedBy().getFirstName() + " " + r.getValidatedBy().getLastName() : null)
                .validationDate(r.getValidationDate())
                .validationNote(r.getValidationNote())
                .fulfilledByEquipmentId(r.getFulfilledByEquipment() != null ? r.getFulfilledByEquipment().getId() : null)
                .fulfilledByEquipmentName(r.getFulfilledByEquipment() != null ? r.getFulfilledByEquipment().getName() : null)
                .build();
    }
}