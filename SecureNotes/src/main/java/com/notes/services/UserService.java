package com.notes.services;

import com.notes.dtos.UserDTO;
import com.notes.entities.Role;
import com.notes.entities.User;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void updateUserRole(Long userId, String roleName);

    List<User> getAllUsers();

    UserDTO getUserById(Long id);

	User findByUsername(String username);
	
	void generatePasswordResetToken(String email);

	void resetPassword(String token, String newPassword);
	
	Optional<User> findByEmail(String email);

	User registerUser(User newUser);

	GoogleAuthenticatorKey generate2FaSecret(Long userId);

	boolean validate2FaCode(Long userId, int code);

	void enable2FA(Long userId);

	void disable2FA(Long userId);
	
	public List<Role> getAllRoles();
	
	public void updateAccountLockStatus(Long id, boolean lock);
	
	public void updateAccountExpiryStatus(Long id, boolean expire);
	
	public void updateAccountEnabledStatus(Long id, boolean enabled);
	
	public void updateAccountCredentialExpiryStatus(Long id, boolean expire);
	
	public void updateAccountPassword(Long id, String password);
}
