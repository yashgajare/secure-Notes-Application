package com.notes.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.notes.entities.Role;
import com.notes.entities.User;
import com.notes.enums.AppRole;
import com.notes.repositories.RoleRepository;
import com.notes.repositories.UserRepository;
import com.notes.security.jwt.JwtUtil;
import com.notes.security.services.UserDetailsImpl;
import com.notes.services.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler{

	@Autowired
	private final UserService userService;
	
	@Autowired
	private final JwtUtil jwtUtil;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Value("${frontend.url}")
	private String frontendUrl;
	
	String username;
	String idAttributeKey;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {
		
		OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
		if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()) || 
				"google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
			DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
			Map<String, Object> attributes = principal.getAttributes();
			String email = attributes.getOrDefault("email", "").toString();
			String name = attributes.getOrDefault("name", "").toString();
			if("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
				username = attributes.getOrDefault("login", "").toString();
				idAttributeKey = "id";
			}else if("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
				username = email.split("@")[0];
				idAttributeKey="sub";
			}else {
				username="";
				idAttributeKey="id";
			}
			
			System.out.println("Hello OAUTH: " + email + " : " + name + " : " + username);
			
			userService.findByEmail(email).
				ifPresentOrElse(user -> {
					DefaultOAuth2User oauthUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())), attributes, idAttributeKey);
					Authentication securityAuth = new OAuth2AuthenticationToken(oauthUser, List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
					SecurityContextHolder.getContext().setAuthentication(securityAuth);
				}, () -> {
					User newUser = new User();
					Optional<Role> role = roleRepository.findByRoleName(AppRole.ROLE_USER);
					if(role.isPresent()) {
						newUser.setRole(role.get());
					}else {
						throw new RuntimeException("Default role not found");
					}
					newUser.setEmail(email);
					newUser.setUsername(username);
					newUser.setSignUpMethod(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
					userService.registerUser(newUser);
					DefaultOAuth2User oauthUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())), attributes, idAttributeKey);
					Authentication securityAuth = new OAuth2AuthenticationToken(oauthUser, List.of(new SimpleGrantedAuthority(newUser.getRole().getRoleName().name())), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
					SecurityContextHolder.getContext().setAuthentication(securityAuth);
				});
		}
		this.setAlwaysUseDefaultTargetUrl(true);
		
		//JWT TOKEN Logic 
		DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();
		
		String email = attributes.get("email").toString();
		System.out.println("OAuth2LoginSuccessHandler: " + username + " : " + email);
		
		Set<SimpleGrantedAuthority> authorities = new HashSet<>(oAuth2User.getAuthorities().stream().map(authority -> new SimpleGrantedAuthority(authority.getAuthority())).collect(Collectors.toList()));
		User user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName().name()));
		
		UserDetailsImpl userDetails = new UserDetailsImpl(null, username, email, null, false, authorities);
//		Generate JWt Token
		String jwtToken = jwtUtil.generateTokenFromUsername(userDetails);
		
//		Redirect to the frontend with jwt token
		String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect").queryParam("token", jwtToken).build().toUriString();
		this.setDefaultTargetUrl(targetUrl);
		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	
	
	
}
