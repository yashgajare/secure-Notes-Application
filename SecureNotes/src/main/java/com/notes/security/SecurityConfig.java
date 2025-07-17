package com.notes.security;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.notes.config.OAuth2LoginSuccessHandler;
import com.notes.entities.Role;
import com.notes.entities.User;
import com.notes.enums.AppRole;
import com.notes.repositories.RoleRepository;
import com.notes.repositories.UserRepository;
import com.notes.security.jwt.AuthEntryPointJwt;
import com.notes.security.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)	
public class SecurityConfig {

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;
	
	@Autowired
	@Lazy
	private OAuth2LoginSuccessHandler auth2LoginSuccessHandler;
	
	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers("/api/auth/public/**"))
		.cors(Customizer.withDefaults()) 
//        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(authorize -> authorize
        		.requestMatchers("/api/admin/**").hasRole("ADMIN")
        		.requestMatchers("/api/csrf-token").permitAll()
        		.requestMatchers("/api/auth/public/**").permitAll()
        		.requestMatchers("/oauth2/**").permitAll()
        		.anyRequest().authenticated())
        .oauth2Login(oauth2 ->{
        	oauth2.successHandler(auth2LoginSuccessHandler);
        })
        .exceptionHandling(exception ->exception.authenticationEntryPoint(unauthorizedHandler))
		.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	
		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	 @Bean
	 public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder encoder) {
	      return args -> {
	            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
	                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));

	            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
	                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

	            if (!userRepository.existsByUsername("user1")) {
	                User user1 = new User("user1", "user1@example.com", encoder.encode("password1"));
	                user1.setAccountNonLocked(false);
	                user1.setAccountNonExpired(true);
	                user1.setCredentialNonExpired(true);
	                user1.setEnabled(true);
	                user1.setCredentialExpiryDate(LocalDate.now().plusYears(1));
	                user1.setAccountExpiryDate(LocalDate.now().plusYears(1));
	                user1.setTwoFactorEnabled(false);
	                user1.setSignUpMethod("email");
	                user1.setRole(userRole);
	                userRepository.save(user1);
	            }
	            
	            if (!userRepository.existsByUsername("admin")) {
	                User admin = new User("admin", "admin@example.com", encoder.encode("adminPass"));
	                admin.setAccountNonLocked(true);
	                admin.setAccountNonExpired(true);
	                admin.setCredentialNonExpired(true);
	                admin.setEnabled(true);
	                admin.setCredentialExpiryDate(LocalDate.now().plusYears(1));
	                admin.setAccountExpiryDate(LocalDate.now().plusYears(1));
	                admin.setTwoFactorEnabled(false);
	                admin.setSignUpMethod("email");
	                admin.setRole(adminRole);
	                userRepository.save(admin);
	            }
	        };
	    }
	 
//	 @Bean
//	    public CorsConfigurationSource corsConfigurationSource() {
//	        CorsConfiguration config = new CorsConfiguration();
//	        config.setAllowedOrigins(List.of("http://localhost:3000"));
//	        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//	        config.setAllowedHeaders(List.of("*"));
//	        config.setAllowCredentials(true);
//
//	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	        source.registerCorsConfiguration("/**", config);
//	        return source;
//	    }
}
