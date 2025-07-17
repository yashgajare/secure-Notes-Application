package com.notes.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notes.entities.AuditLog;
import com.notes.services.AuditLogService;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

	@Autowired
	private AuditLogService auditLogService;
	
	@GetMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AuditLog>getAuditLogs(){
		return auditLogService.getAuditLogs();
	}
	
	@GetMapping("/note/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AuditLog> getNoteAuditLogs(@PathVariable Long id){
		return auditLogService.getAllAuditLogForNoteId(id);
	}
}
