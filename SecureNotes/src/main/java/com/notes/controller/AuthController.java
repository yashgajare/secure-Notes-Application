package com.notes.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notes.entities.Role;
import com.notes.entities.User;
import com.notes.enums.AppRole;
import com.notes.repositories.RoleRepository;
import com.notes.repositories.UserRepository;
import com.notes.security.jwt.JwtUtil;
import com.notes.security.request.LoginRequest;
import com.notes.security.request.SignupRequest;
import com.notes.security.response.LoginResponse;
import com.notes.security.response.MessageResponse;
import com.notes.security.response.UserInfoResponse;
import com.notes.security.services.UserDetailsImpl;
import com.notes.services.TotpService;
import com.notes.services.UserService;
import com.notes.utils.AuthUtil;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private JwtUtil jwtUtils;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	private TotpService totpService;

	@PostMapping("/public/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		} catch (AuthenticationException e) {
			Map<String, Object> map = new HashMap<>();
			map.put("message", "Bad Credential");
			map.put("status", "false");

			return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());
		LoginResponse response = new LoginResponse(jwtToken, userDetails.getUsername(), roles);

		return ResponseEntity.ok(response);

	}

	@PostMapping("/public/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

		if (userRepository.existsByUsername(signupRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signupRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
				encoder.encode(signupRequest.getPassword()));

		Set<String> strRoles = signupRequest.getRole();
		Role role;

		if (strRoles == null || strRoles.isEmpty()) {
			role = roleRepository.findByRoleName(AppRole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found"));
		} else {
			String roleStr = strRoles.iterator().next();
			if (roleStr.equals("admin")) {
				role = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			} else {
				role = roleRepository.findByRoleName(AppRole.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			}
		}
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialNonExpired(true);
		user.setEnabled(true);
		user.setCredentialExpiryDate(LocalDate.now().plusYears(1));
		user.setAccountExpiryDate(LocalDate.now().plusYears(1));
		user.setTwoFactorEnabled(false);
		user.setSignUpMethod("email");

		user.setRole(role);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));

	}

	@GetMapping("/user")
	public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
		User user = userService.findByUsername(userDetails.getUsername());

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		UserInfoResponse response = new UserInfoResponse(user.getUserId(), user.getUsername(), user.getEmail(),
				user.isAccountNonLocked(), user.isAccountNonExpired(), user.isCredentialNonExpired(), user.isEnabled(),
				user.getCredentialExpiryDate(), user.getAccountExpiryDate(), user.isTwoFactorEnabled(), roles);

		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/username")
	public String getCurrentUsername(@AuthenticationPrincipal UserDetails userDetails) {
		return (userDetails != null ? userDetails.getUsername() : "");
	}

	@PostMapping("/public/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestParam String email) {
		try {
			userService.generatePasswordResetToken(email);
			return ResponseEntity.ok(new MessageResponse("Password reset email send Successfully."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new MessageResponse("Error sending password reset email!"));
		}
	}

	@PostMapping("/public/reset-password")
	public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
		try {
			userService.resetPassword(token, newPassword);
			return ResponseEntity.ok(new MessageResponse("Password reset Successfully."));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
		}
	}

	@PostMapping("/enable-2fa")
	public ResponseEntity<String> enable2FA() {
		Long userId = authUtil.loggedInUserId();
		GoogleAuthenticatorKey secret = userService.generate2FaSecret(userId);
		String qrCodeUrl = totpService.getQrCodeUrl(secret, userService.getUserById(userId).getUserName());
		return ResponseEntity.ok(qrCodeUrl);
	}

	@PostMapping("/disable-2fa")
	public ResponseEntity<String> disable2FA() {
		Long userId = authUtil.loggedInUserId();
		userService.disable2FA(userId);
		return ResponseEntity.ok("2FA Disabled");
	}

	@PostMapping("/verify-2fa")
	public ResponseEntity<String> verify2FA(@RequestParam int code) {
		Long userId = authUtil.loggedInUserId();
		boolean isValid = userService.validate2FaCode(userId, code);
		if (isValid) {
			userService.enable2FA(userId);
			return ResponseEntity.ok("2FA Verified");
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA Code");
		}
	}

	@GetMapping("/user/2fa-status")
	public ResponseEntity<?> get2FaStatus() {
		User user = authUtil.loggedInUser();
		if (user != null) {
			return ResponseEntity.ok().body(Map.of("is2faEnabled", user.isTwoFactorEnabled()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}
	}

	@PostMapping("/public/verify-2fa-login")
	public ResponseEntity<String> verify2FALogin(@RequestParam int code, @RequestParam String jwtToken) {
		String username = jwtUtils.getUsernamefromJwtToken(jwtToken);
		User user = userService.findByUsername(username);
		boolean isValid = userService.validate2FaCode(user.getUserId(), code);
		if (isValid) {
			return ResponseEntity.ok("2FA Verified");
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA Code");
		}
	}
}
