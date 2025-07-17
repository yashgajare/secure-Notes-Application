package com.notes.security.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

	private String jwtToken;
	private String username;
	private List<String> roles;
}
