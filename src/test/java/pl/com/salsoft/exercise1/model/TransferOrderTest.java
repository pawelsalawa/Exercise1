package pl.com.salsoft.exercise1.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

public class TransferOrderTest {
	@Test
	public void testClone() {
		// Given
		final var order = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		// When
		final var result = order.clone();

		// Then
		assertEquals(order, result);
	}

	@Test
	public void testJsonCreator() {
		// Given
		final var expected = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		// When
		final var created = new TransferOrder(123L, "123", "234", BigDecimal.TEN, TransferStatus.PLANNED);

		// Then
		assertEquals(expected, created);
	}

	@Test
	public void testMergeNonNull() {
		// Given
		final var initial = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		final var expected = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("0")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		final var updateToApply = TransferOrder.builder()
				.sourceAccount("0")
				.build();

		// When
		final var result = initial.mergeNonNull(updateToApply);

		// Then
		assertEquals(expected, result);
	}

	@Test
	public void testMergeNonNullWithAll() {
		// Given
		final var initial = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		final var expected = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.ONE)
				.sourceAccount("0")
				.targetAccount("0")
				.status(TransferStatus.REJECTED)
				.build();

		final var updateToApply = TransferOrder.builder()
				.id(1L)
				.amount(BigDecimal.ONE)
				.sourceAccount("0")
				.targetAccount("0")
				.status(TransferStatus.REJECTED)
				.build();

		// When
		final var result = initial.mergeNonNull(updateToApply);

		// Then
		assertEquals(expected, result);
	}

	@Test
	public void testMergeNonNullWithId() {
		// Given
		final var initial = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		final var expected = TransferOrder.builder()
				.id(123L)
				.amount(BigDecimal.TEN)
				.sourceAccount("0")
				.targetAccount("234")
				.status(TransferStatus.PLANNED)
				.build();

		final var updateToApply = TransferOrder.builder()
				.id(1L)
				.sourceAccount("0")
				.build();

		// When
		final var result = initial.mergeNonNull(updateToApply);

		// Then
		assertEquals(expected, result);
	}
}
