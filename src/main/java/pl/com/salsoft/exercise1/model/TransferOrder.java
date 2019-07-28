package pl.com.salsoft.exercise1.model;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Domain class representing money transfer order. It's immutable class to
 * guarantee thread-safety and consistency during concurrent reads/writes.
 * It uses Builder pattern from Lombok to accomplish easy creation,
 * cloning and partial merging.
 */
@Builder(toBuilder = true)
@Getter
@EqualsAndHashCode
@ToString
public final class TransferOrder {
	private final Long id;
	private final String sourceAccount;
	private final String targetAccount;
	private final BigDecimal amount;
	private final TransferStatus status;

	/**
	 * Explicit, all-fields constructor for Jackson deserializer to work with
	 * immutable class such as this.
	 */
	@JsonCreator
	public TransferOrder(@JsonProperty("id") final Long id, @JsonProperty("sourceAccount") final String sourceAccount,
			@JsonProperty("targetAccount") final String targetAccount, @JsonProperty("amount") final BigDecimal amount,
			@JsonProperty("status") final TransferStatus status) {
		this.id = id;
		this.sourceAccount = sourceAccount;
		this.targetAccount = targetAccount;
		this.amount = amount;
		this.status = status;
	}

	/**
	 * Creates a shallow copy of this object.
	 */
	@Override
	public TransferOrder clone() {
		return toBuilder().build();
	}

	/**
	 * Copies this instance (shallow copy) and overwrite field values with
	 * respective values (only if they are not null) from the order passed in
	 * parameters.
	 *
	 * @param order The other order to take non-null values from.
	 * @return New order object with values from the other order merged with this
	 *         order's values.
	 */
	public @NonNull TransferOrder mergeNonNull(@NonNull final TransferOrder order) {
		final TransferOrderBuilder builder = this.toBuilder();
		Optional.ofNullable(order.sourceAccount).ifPresent(builder::sourceAccount);
		Optional.ofNullable(order.targetAccount).ifPresent(builder::targetAccount);
		Optional.ofNullable(order.amount).ifPresent(builder::amount);
		Optional.ofNullable(order.status).ifPresent(builder::status);
		return builder.build();
	}
}
