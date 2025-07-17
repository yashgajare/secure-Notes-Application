package com.notes.controller;

import com.notes.dtos.UserDTO;
import com.notes.entities.Role;
import com.notes.entities.User;
import com.notes.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")

public class AdminController {

	@Autowired
	private UserService userService;

	@GetMapping("/getusers")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<List<User>> getAllUsers() {
		return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
	}

	@PutMapping("/update-role")
	public ResponseEntity<String> updateUserRole(@RequestParam Long userId, @RequestParam String roleName) {
		userService.updateUserRole(userId, roleName);
		return ResponseEntity.ok("User role updated");
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
		return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
	}

	@GetMapping("/roles")
	public List<Role> getAllRoles() {
		return userService.getAllRoles();
	}

	@PutMapping("/update-password")
	public ResponseEntity<String> updateAccountPassword(Long userId, String password) {
		try {
			userService.updateAccountPassword(userId, password);
			return ResponseEntity.ok("Password updated");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PutMapping("/update-lock-status")
	public ResponseEntity<String> updateAccountLockStatus(Long userId, boolean lock) {
		userService.updateAccountLockStatus(userId, lock);
		return ResponseEntity.ok("Account lock status updated");
	}

	@PutMapping("/update-expiry-status")
	public ResponseEntity<String> updateAccountExpiryStatus(Long userId, boolean expire) {
		userService.updateAccountExpiryStatus(userId, expire);
		return ResponseEntity.ok("Account expiry status updated");
	}

	@PutMapping("/update-enabled-status")
	public ResponseEntity<String> updateAccountEnabledStatus(Long userId, boolean enabled) {
		userService.updateAccountEnabledStatus(userId, enabled);
		return ResponseEntity.ok("Account enabled status updated");
	}

	@PutMapping("/update-credentials-expiry-status")
	public ResponseEntity<String> updateAccountCredentialExpiryStatus(Long userId, boolean expire) {
		userService.updateAccountCredentialExpiryStatus(userId, expire);
		return ResponseEntity.ok("Credentials expiry status updated");
	}

}
