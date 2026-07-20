package com.samaanlink.common.audit;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

/**
 * Base class for entities that need createdAt/By and updatedAt/By tracking. Populated by
 * {@link AuditingEntityListener}, activated via {@code @EnableJpaAuditing} on
 * {@code SamaanLinkApplication}. {@code createdBy}/{@code updatedBy} are the acting user's
 * Identity-module {@code userId} (a plain UUID reference — see the module-boundary rule that
 * cross-module references are UUIDs, never entities).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;

	@CreatedBy
	private UUID createdBy;

	@LastModifiedBy
	private UUID updatedBy;

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public UUID getUpdatedBy() {
		return updatedBy;
	}
}
