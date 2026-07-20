package com.samaanlink.identity.application;

import java.util.UUID;

/**
 * The only way another module may interact with Identity. No other module may reference
 * {@code com.samaanlink.identity.domain} or {@code com.samaanlink.identity.infrastructure}
 * directly - Spring Modulith's boundary verification enforces this.
 */
public interface IdentityFacade {

	UserSummary registerUser(RegisterUserCommand command);

	AuthResult login(LoginCommand command);

	AuthResult refresh(RefreshTokenCommand command);

	void logout(RefreshTokenCommand command);

	void requestPasswordReset(RequestPasswordResetCommand command);

	void resetPassword(ResetPasswordCommand command);

	void activateUser(UUID userId);

	void suspendUser(UUID userId);

	UserSummary findUserSummary(UUID userId);
}
