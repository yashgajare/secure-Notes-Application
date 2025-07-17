package com.notes.services.impl;

import org.springframework.stereotype.Service;

import com.notes.services.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

@Service
public class TotpServiceImpl implements TotpService {

	private final GoogleAuthenticator gAuth;

	public TotpServiceImpl() {
		this.gAuth = new GoogleAuthenticator();
	}
	
	public TotpServiceImpl(GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}
	
	@Override
	public GoogleAuthenticatorKey generateSecret() {
		return gAuth.createCredentials();
	}
	
	@Override
	public String getQrCodeUrl(GoogleAuthenticatorKey secret, String username) {
		return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Secure Notes Applocation", username, secret);
	}
	
	@Override
	public boolean verifyCode(String secret, int code) {
		return gAuth.authorize(secret, code);
	}
}
