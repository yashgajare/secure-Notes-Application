package com.notes.services;

import java.util.List;

import com.notes.entities.Note;

public interface NotesService {

	public Note createNote(String content, String username);
	
	public List<Note> getNotes(String username);
	
	public void deleteNote(Long id);
	
	public Note updateNote(Long id, String content);
	
}
