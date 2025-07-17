package com.notes.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notes.entities.Role;
import com.notes.enums.AppRole;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByRoleName(AppRole appRole);
	
}
