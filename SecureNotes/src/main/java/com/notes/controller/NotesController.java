package com.notes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notes.entities.Note;
import com.notes.services.NotesService;

@RestController
@RequestMapping("/api/notes")
public class NotesController {
	
	@Autowired
	private NotesService notesService;
	
	@PostMapping
	public Note CreateNote(@RequestBody String content, @AuthenticationPrincipal UserDetails userDetails) {
		String username = userDetails.getUsername();
		System.out.println(username);
		return notesService.createNote(content, username);
	}
	
	@GetMapping
	public List<Note> getNotes(@AuthenticationPrincipal UserDetails userDetails){
		return notesService.getNotes(userDetails.getUsername());
	}
	
	@PutMapping("/{id}")
	public Note updateNote(@PathVariable Long id, @RequestBody String content, @AuthenticationPrincipal UserDetails userDetails) {
		return notesService.updateNote(id, content);
	}
	
	@DeleteMapping("/{id}")
	public void deleteNote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
		notesService.deleteNote(id);
	}
}
