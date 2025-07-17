package com.notes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notes.entities.Note;

public interface NotesRepository extends JpaRepository<Note, Long>{
	
	public List<Note> findByOwnerUsername(String username);
}
