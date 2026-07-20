package com.samaanlink.identity.application.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samaanlink.identity.application.AuthResult;
import com.samaanlink.identity.application.IdentityException;
import com.samaanlink.identity.application.IdentityFacade;
import com.samaanlink.identity.application.LoginCommand;
import com.samaanlink.identity.application.RefreshTokenCommand;
import com.samaanlink.identity.application.RegisterUserCommand;
import com.samaanlink.identity.application.RequestPasswordResetCommand;
import com.samaanlink.identity.application.ResetPasswordCommand;
import com.samaanlink.identity.application.UserSummary;
import com.samaanlink.identity.domain.PasswordResetToken;
import com.samaanlink.identity.domain.RefreshToken;
import com.samaanlink.identity.domain.Role;
import com.samaanlink.identity.domain.User;
import com.samaanlink.identity.infrastructure.PasswordResetTokenRepository;
import com.samaanlink.identity.infrastructure.RefreshTokenRepository;
import com.samaanlink.identity.infrastructure.RoleRepository;
import com.samaanlink.identity.infrastructure.UserRepository;
import com.samaanlink.identity.infrastructure.security.JwtTokenProvider;

@Service
public class IdentityFacadeImpl implements IdentityFacade {

	private static final Logger log = LoggerFactory.getLogger(IdentityFacadeImpl.class);
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public IdentityFacadeImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordResetTokenRepository passwordResetTokenRepository,
			RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	@Transactional
	public UserSummary registerUser(RegisterUserCommand command) {
		if (userRepository.existsByEmailIgnoreCase(command.email())) {
			throw new IdentityException("An account with this email already exists");
		}
		Role role = roleRepository.findByName(command.roleName())
				.orElseThrow(() -> new IdentityException("Unknown role: " + command.roleName()));

		User user = User.create(command.email(), passwordEncoder.encode(command.rawPassword()),
				command.firstName(), command.lastName(), command.phone(), role);
		user = userRepository.save(user);
		return toSummary(user);
	}

	@Override
	@Transactional
	public AuthResult login(LoginCommand command) {
		User user = userRepository.findByEmailIgnoreCase(command.email())
				.orElseThrow(() -> new IdentityException("Invalid email or password"));

		if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
			throw new IdentityException("Invalid email or password");
		}
		if (!user.isActive()) {
			throw new IdentityException("This account is not active");
		}
		return issueTokens(user);
	}

	@Override
	@Transactional
	public AuthResult refresh(RefreshTokenCommand command) {
		String hash = sha256(command.refreshToken());
		RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
				.orElseThrow(() -> new IdentityException("Invalid refresh token"));
		if (!stored.isUsable(Instant.now())) {
			throw new IdentityException("Refresh token is expired or revoked");
		}
		User user = userRepository.findById(stored.getUserId())
				.orElseThrow(() -> new IdentityException("Invalid refresh token"));
		if (!user.isActive()) {
			throw new IdentityException("This account is not active");
		}

		stored.revoke();
		return issueTokens(user);
	}

	@Override
	@Transactional
	public void logout(RefreshTokenCommand command) {
		refreshTokenRepository.findByTokenHash(sha256(command.refreshToken()))
				.ifPresent(RefreshToken::revoke);
	}

	@Override
	@Transactional
	public void requestPasswordReset(RequestPasswordResetCommand command) {
		userRepository.findByEmailIgnoreCase(command.email()).ifPresent(user -> {
			String rawToken = randomToken();
			passwordResetTokenRepository.save(
					PasswordResetToken.issue(user.getId(), sha256(rawToken), Instant.now().plusSeconds(3600)));
			// The Notification module (Phase 6) will deliver this by email; until then it is
			// logged so the reset flow is testable end-to-end.
			log.info("Password reset token for {}: {}", user.getEmail(), rawToken);
		});
		// Deliberately no exception when the email is unknown - do not reveal account existence.
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordCommand command) {
		PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(sha256(command.resetToken()))
				.orElseThrow(() -> new IdentityException("Invalid or expired reset token"));
		if (!token.isUsable(Instant.now())) {
			throw new IdentityException("Invalid or expired reset token");
		}
		User user = userRepository.findById(token.getUserId())
				.orElseThrow(() -> new IdentityException("Invalid or expired reset token"));

		user.changePassword(passwordEncoder.encode(command.newPassword()));
		token.markUsed(Instant.now());
	}

	@Override
	@Transactional
	public void activateUser(UUID userId) {
		userRepository.findById(userId).orElseThrow(() -> new IdentityException("User not found")).activate();
	}

	@Override
	@Transactional
	public void suspendUser(UUID userId) {
		userRepository.findById(userId).orElseThrow(() -> new IdentityException("User not found")).suspend();
	}

	@Override
	@Transactional(readOnly = true)
	public UserSummary findUserSummary(UUID userId) {
		return userRepository.findById(userId).map(this::toSummary)
				.orElseThrow(() -> new IdentityException("User not found"));
	}

	private AuthResult issueTokens(User user) {
		String accessToken = jwtTokenProvider.generateAccessToken(user);
		String refreshToken = jwtTokenProvider.generateRefreshToken(user);
		refreshTokenRepository.save(
				RefreshToken.issue(user.getId(), sha256(refreshToken), jwtTokenProvider.refreshTokenExpiry()));
		return new AuthResult(accessToken, refreshToken, jwtTokenProvider.accessTokenTtlSeconds(), user.getId(),
				user.getRole().getName());
	}

	private UserSummary toSummary(User user) {
		return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
				user.getRole().getName(), user.getStatus().name());
	}

	private static String randomToken() {
		byte[] bytes = new byte[32];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
