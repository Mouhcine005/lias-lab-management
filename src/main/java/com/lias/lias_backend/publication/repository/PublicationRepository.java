package com.lias.lias_backend.publication.repository;

import com.lias.lias_backend.publication.entity.Publication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByMemberId(Long memberId);
    List<Publication> findByYear(Integer year);
    List<Publication> findByTeam(String team);
    List<Publication> findByMemberIdOrderByYearDesc(Long memberId);
}