package com.samaanlink;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

/**
 * Throwaway: boots the real app (default application.properties, port 8088, real docker-compose
 * Postgres) on the already-resolved test classpath, for manual browser verification of the Task 7
 * thin slice. Not part of the module - delete after use.
 */
class ManualServerLauncher {

	@Test
	void run() throws InterruptedException {
		SpringApplication.run(SamaanLinkApplication.class, "--server.port=9090");
		new CountDownLatch(1).await();
	}
}
