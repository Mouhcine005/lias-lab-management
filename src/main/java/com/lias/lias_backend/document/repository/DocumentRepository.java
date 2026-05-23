package com.lias.lias_backend.document.repository;

import com.lias.lias_backend.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEventId(Long eventId);
    List<Document> findByUploadedById(Long memberId);
    List<Document> findByType(Document.DocumentType type);
}