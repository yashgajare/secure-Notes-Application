package com.notes.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Data
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(columnNames = "email"),
		@UniqueConstraint(columnNames = "username")
})
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;
	
	@NotBlank
	@Size(max = 20)
	@Column(name = "username")
	private String username;
	
	@NotBlank
	@Size(max = 50)
	@Column(name = "email")
	private String email;
	
	@Size(max = 120)
	@JsonIgnore
	private String password;
	
	private boolean accountNonLocked = true;
	
	private boolean accountNonExpired = true;
	
	private boolean credentialNonExpired = true;
	
	private boolean enabled = true;
	
	private LocalDate credentialExpiryDate;
	private LocalDate accountExpiryDate;
	
	private String twoFactorSecret;
	
	private boolean isTwoFactorEnabled = false;
	
	private String signUpMethod;
	
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinColumn(name="role_id", referencedColumnName = "role_id")
	@JsonBackReference
	@ToString.Exclude
	private Role role;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdDate;
	
	@UpdateTimestamp
	private LocalDateTime updatedDate;
	
	public User(String username, String email, String password) {
		this.username=username;
		this.email=email;
		this.password=password;
	}
	
	public User(String username, String email) {
		this.username=username;
		this.email=email;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof User)) return false;
		return userId!=null && userId.equals(((User) o).getUserId());
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
