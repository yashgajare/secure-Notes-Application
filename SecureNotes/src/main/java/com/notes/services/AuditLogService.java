package com.notes.services;

import java.util.List;

import com.notes.entities.AuditLog;
import com.notes.entities.Note;

public interface AuditLogService {

	public void logNoteCreation(String username, Note note);
	
	public void logNoteUpdation(String username, Note note);
	
	public void logNoteDeletion(String username, Long noteId);

	public List<AuditLog> getAuditLogs();

	public List<AuditLog> getAllAuditLogForNoteId(Long id);
	
}
