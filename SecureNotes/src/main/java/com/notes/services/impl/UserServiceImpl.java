package com.notes.services.impl;

import com.notes.dtos.UserDTO;
import com.notes.enums.AppRole;
import com.notes.entities.PasswordResetToken;
import com.notes.entities.Role;
import com.notes.entities.User;
import com.notes.repositories.PasswordResetTokenRepository;
import com.notes.repositories.RoleRepository;
import com.notes.repositories.UserRepository;
import com.notes.services.TotpService;
import com.notes.services.UserService;
import com.notes.utils.EmailService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    
	@Value("${frontend.url}")
	String frontendUrl;
	
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @Autowired
    private TotpService totpService;

    @Override
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = AppRole.valueOf(roleName);
        Role role = roleRepository.findByRoleName(appRole)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public UserDTO getUserById(Long id) {
//        return userRepository.findById(id).orElseThrow();
        User user = userRepository.findById(id).orElseThrow();
        return convertToDto(user);
    }

    private UserDTO convertToDto(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.isAccountNonLocked(),
                user.isAccountNonExpired(),
                user.isCredentialNonExpired(),
                user.isEnabled(),
                user.getCredentialExpiryDate(),
                user.getAccountExpiryDate(),
                user.getTwoFactorSecret(),
                user.isTwoFactorEnabled(),
                user.getSignUpMethod(),
                user.getRole(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }
    
    @Override
    public User findByUsername(String username) {

    	Optional<User> user = userRepository.findByUsername(username);
    	return user.orElseThrow(() -> new UsernameNotFoundException("Error: Username not found!"));
    }


	@Override
	public void generatePasswordResetToken(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
		String token = UUID.randomUUID().toString();
		Instant expire = Instant.now().plus(24, ChronoUnit.HOURS);
		PasswordResetToken resetToken = new PasswordResetToken(token, expire, user);
		tokenRepository.save(resetToken);
		
		String resetUrl = frontendUrl + "/reset-password?token=" + token;
		//logic to send email
		emailService.sendPasswordResetEmail(email, resetUrl);
	}


	@Override
	public void resetPassword(String token, String newPassword) {
		PasswordResetToken resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid Password reset token!"));
		
		if(resetToken.isUsed()) {
			throw new RuntimeException("Password reset token has already been used");
		}
		
		if(resetToken.getExpire().isBefore(Instant.now()))
		{
			throw new RuntimeException("Password reset token has been expired");
		}
		
		User user = resetToken.getUser();
		user.setPassword(encoder.encode(newPassword));
		userRepository.save(user);
		resetToken.setUsed(true);
		tokenRepository.save(resetToken);
	}


	@Override
	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}


	@Override
	public User registerUser(User user) {
		if(user.getPassword()!=null) {
			user.setPassword(encoder.encode(user.getPassword()));
		}
		return userRepository.save(user);
		
	}
	
	@Override
	public GoogleAuthenticatorKey generate2FaSecret(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		GoogleAuthenticatorKey key = totpService.generateSecret();
		user.setTwoFactorSecret(key.getKey());
		userRepository.save(user);
		return key;
	}
	
	@Override
	public boolean validate2FaCode(Long userId, int code) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		return totpService.verifyCode(user.getTwoFactorSecret(), code);
	}
	
	@Override
	public void enable2FA(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		user.setTwoFactorEnabled(true);
		userRepository.save(user);
	}
	
	@Override
	public void disable2FA(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		user.setTwoFactorEnabled(false);
		userRepository.save(user);
	}
	
	@Override
	public List<Role> getAllRoles() {
		return roleRepository.findAll();
	}

	@Override
	public void updateAccountLockStatus(Long id, boolean lock) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setAccountNonLocked(!lock);
		userRepository.save(user);
	}

	@Override
	public void updateAccountExpiryStatus(Long id, boolean expire) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setAccountNonExpired(!expire);
		userRepository.save(user);
	}

	@Override
	public void updateAccountEnabledStatus(Long id, boolean enabled) {
		System.out.println("Enabled: " + enabled);
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setEnabled(enabled);
		userRepository.save(user);
	}

	@Override
	public void updateAccountCredentialExpiryStatus(Long id, boolean expire) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setCredentialNonExpired(!expire);
		userRepository.save(user);
	}

	@Override
	public void updateAccountPassword(Long id, String password) {
		try {
			User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
			user.setPassword(encoder.encode(password));
			userRepository.save(user);
		} catch (Exception e) {
			throw new RuntimeException("Failed to update password");
		}
	}

}
