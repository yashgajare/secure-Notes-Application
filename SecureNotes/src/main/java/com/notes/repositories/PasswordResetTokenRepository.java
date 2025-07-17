package com.notes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notes.entities.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
	
	Optional<PasswordResetToken> findByToken(String token);
}
