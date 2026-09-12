package com.lias.lias_backend.membershiprequest.repository;

import com.lias.lias_backend.membershiprequest.entity.MembershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRequestRepository extends JpaRepository<MembershipRequest, Long> {
    List<MembershipRequest> findByStatusOrderBySubmittedAtDesc(MembershipRequest.RequestStatus status);
    List<MembershipRequest> findAllByOrderBySubmittedAtDesc();
    boolean existsByEmailAndStatus(String email, MembershipRequest.RequestStatus status);
}
