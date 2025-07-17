package com.notes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notes.entities.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{

	List<AuditLog> findByNoteId(Long noteId);

}
