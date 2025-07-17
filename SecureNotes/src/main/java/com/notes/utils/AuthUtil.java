package com.notes.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.notes.entities.User;
import com.notes.repositories.UserRepository;

@Component
public class AuthUtil {

	@Autowired
	private UserRepository userRepository;
	
	public Long loggedInUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
		return user.getUserId();
	}
	
	public User loggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
		return user;
	}
	
}
