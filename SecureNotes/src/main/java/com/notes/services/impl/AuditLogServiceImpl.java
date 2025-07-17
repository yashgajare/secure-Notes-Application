package com.notes.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.notes.entities.AuditLog;
import com.notes.entities.Note;
import com.notes.repositories.AuditLogRepository;
import com.notes.services.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

	@Autowired
	private AuditLogRepository auditLogRepository;
	
	@Override
	public void logNoteCreation(String username, Note note) {
		AuditLog log = new AuditLog();
		log.setAction("CREATE");
		log.setUsername(username);
		log.setNoteId(note.getId());
		log.setContent(note.getContent());
		log.setTimeStamp(LocalDateTime.now());
		auditLogRepository.save(log);
	}

	@Override
	public void logNoteUpdation(String username, Note note) {
		AuditLog log = new AuditLog();
		log.setAction("UPDATE");
		log.setUsername(username);
		log.setNoteId(note.getId());
		log.setContent(note.getContent());
		log.setTimeStamp(LocalDateTime.now());
		auditLogRepository.save(log);
	}

	@Override
	public void logNoteDeletion(String username, Long noteId) {
		AuditLog log = new AuditLog();
		log.setAction("DELETE");
		log.setUsername(username);
		log.setNoteId(noteId);
		log.setTimeStamp(LocalDateTime.now());
		auditLogRepository.save(log);
	}

	@Override
	public List<AuditLog> getAuditLogs() {
		return auditLogRepository.findAll();
	}

	@Override
	public List<AuditLog> getAllAuditLogForNoteId(Long id) {
		return auditLogRepository.findByNoteId(id);
	}

}
