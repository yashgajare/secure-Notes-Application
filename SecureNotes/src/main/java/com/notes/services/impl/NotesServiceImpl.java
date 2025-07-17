package com.notes.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.notes.entities.Note;
import com.notes.repositories.NotesRepository;
import com.notes.services.AuditLogService;
import com.notes.services.NotesService;

@Service
public class NotesServiceImpl implements NotesService {

	@Autowired
	private NotesRepository notesRepository;
	
	@Autowired
	private AuditLogService auditLogService;
	
	@Override
	public Note createNote(String content, String username) {
		Note note = new Note();
		note.setContent(content);
		note.setOwnerUsername(username);
		Note savedNote = notesRepository.save(note);
		auditLogService.logNoteCreation(username, savedNote);
		return savedNote;
	}

	@Override
	public List<Note> getNotes(String username) {
		if(username.isEmpty()) {
			return null;
		}
		return notesRepository.findByOwnerUsername(username);
	}

	@Override
	public void deleteNote(Long id) {
		Note note = notesRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
		notesRepository.deleteById(id);
		auditLogService.logNoteDeletion(note.getOwnerUsername(), id);
	}

	@Override
	public Note updateNote(Long id, String content) {
		  Note note = notesRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
		  note.setContent(content);
		  Note updatedNote = notesRepository.save(note);
		  auditLogService.logNoteUpdation(note.getOwnerUsername(), updatedNote);
		  return updatedNote;
	}

}
