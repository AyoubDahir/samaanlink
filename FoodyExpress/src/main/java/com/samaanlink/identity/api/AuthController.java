package com.samaanlink.identity.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samaanlink.identity.application.IdentityFacade;
import com.samaanlink.identity.application.LoginCommand;
import com.samaanlink.identity.application.RefreshTokenCommand;
import com.samaanlink.identity.application.RequestPasswordResetCommand;
import com.samaanlink.identity.application.ResetPasswordCommand;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final IdentityFacade identityFacade;

	public AuthController(IdentityFacade identityFacade) {
		this.identityFacade = identityFacade;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		var result = identityFacade.login(new LoginCommand(request.email(), request.password()));
		return ResponseEntity.ok(AuthResponse.from(result));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		var result = identityFacade.refresh(new RefreshTokenCommand(request.refreshToken()));
		return ResponseEntity.ok(AuthResponse.from(result));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
		identityFacade.logout(new RefreshTokenCommand(request.refreshToken()));
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/password-reset/request")
	public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestRequest request) {
		identityFacade.requestPasswordReset(new RequestPasswordResetCommand(request.email()));
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/password-reset/confirm")
	public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
		identityFacade.resetPassword(new ResetPasswordCommand(request.resetToken(), request.newPassword()));
		return ResponseEntity.noContent().build();
	}
}
