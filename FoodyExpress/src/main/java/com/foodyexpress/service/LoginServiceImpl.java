package com.foodyexpress.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foodyexpress.exception.LoginException;
import com.foodyexpress.model.Admin;
import com.foodyexpress.model.CurrentUserSession;
import com.foodyexpress.model.Customer;
import com.foodyexpress.model.Login;
import com.foodyexpress.model.LoginDTO;
import com.foodyexpress.model.LoginResponseDTO;
import com.foodyexpress.repository.CustomerRepo;
import com.foodyexpress.repository.AdminRepo;
import com.foodyexpress.repository.CurrentUserSessionRepo;

import java.security.SecureRandom;

@Service
public class LoginServiceImpl implements LoginService {

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private AdminRepo adminRepo;

	@Autowired
	private CurrentUserSessionRepo sessionRepo;

	private static final String KEY_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final SecureRandom RANDOM = new SecureRandom();

	private static String randomKey(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(KEY_ALPHABET.charAt(RANDOM.nextInt(KEY_ALPHABET.length())));
		}
		return sb.toString();
	}

	@Override
	public LoginResponseDTO loginAccount(LoginDTO loginDTO) throws LoginException {

		if (loginDTO.getRole().equalsIgnoreCase("customer")) {

			Customer customer = customerRepo.findByEmail(loginDTO.getEmail());
			if (customer == null)
				throw new LoginException("Invalid email");

			if (customer.getPassword().equals(loginDTO.getPassword())) {

				CurrentUserSession cuurSession = sessionRepo.findByEmail(loginDTO.getEmail());

				if (cuurSession != null)
					return new LoginResponseDTO("Login Sucessufull!", cuurSession.getPrivateKey(), "customer",
							customer.getCustomerId());

				CurrentUserSession currentUserSession = new CurrentUserSession();
				currentUserSession.setEmail(loginDTO.getEmail());
				currentUserSession.setLoginDateTime(LocalDateTime.now());
				currentUserSession.setRole("customer");
				String privateKey = randomKey(6);
				currentUserSession.setPrivateKey(privateKey);

				sessionRepo.save(currentUserSession);
				return new LoginResponseDTO("Login Sucessufull!", privateKey, "customer", customer.getCustomerId());
			} else {
				throw new LoginException("Please Enter a valid password");
			}

		} else if (loginDTO.getRole().equalsIgnoreCase("admin")) {
			Admin admin = adminRepo.findByEmail(loginDTO.getEmail());
			if (admin == null)
				throw new LoginException("Invalid email");

			if (admin.getPassword().equals(loginDTO.getPassword())) {

				CurrentUserSession cuurSession = sessionRepo.findByEmail(loginDTO.getEmail());

				if (cuurSession != null)
					return new LoginResponseDTO("Login Sucessufull!", cuurSession.getPrivateKey(), "admin", null);

				CurrentUserSession currentUserSession = new CurrentUserSession();
				currentUserSession.setEmail(loginDTO.getEmail());
				currentUserSession.setLoginDateTime(LocalDateTime.now());
				currentUserSession.setRole("admin");
				String privateKey = randomKey(6);
				currentUserSession.setPrivateKey(privateKey);

				sessionRepo.save(currentUserSession);
				return new LoginResponseDTO("Login Sucessufull!", privateKey, "admin", null);
			} else {
				throw new LoginException("Please Enter a valid password");
			}
		}
		throw new LoginException("Invalid role");
	}

	@Override
	public String logoutAccount(String role, String key) throws LoginException {

		if (role.equalsIgnoreCase("customer")) {

			CurrentUserSession currSession = sessionRepo.findByPrivateKey(key);
			if (currSession == null)
				throw new LoginException("Invalid key");

			if (currSession.getRole().equalsIgnoreCase("customer")) {

				sessionRepo.delete(currSession);
				return "Logged Out!";

			} else
				throw new LoginException("Invalid role");

		} else if (role.equalsIgnoreCase("admin")) {

			CurrentUserSession currSession = sessionRepo.findByPrivateKey(key);
			if (currSession == null)
				throw new LoginException("Invalid key");

			if (currSession.getRole().equalsIgnoreCase("admin")) {

				sessionRepo.delete(currSession);
				return "Logged Out!";

			} else
				throw new LoginException("Invalid role");

		} else
			throw new LoginException("Invalid role");
	}
}
