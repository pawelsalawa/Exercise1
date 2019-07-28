package pl.com.salsoft.exercise1.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import pl.com.salsoft.exercise1.dao.TransferOrderDao;
import pl.com.salsoft.exercise1.model.TransferOrder;
import pl.com.salsoft.exercise1.model.TransferStatus;

@RunWith(MockitoJUnitRunner.class)
public class TransferServiceTest {
	class AppTestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(TransferOrderDao.class).toInstance(transferOrderDao);
			bind(TransferService.class);
		}
	}

	private TransferService transferService;

	@Mock
	private TransferOrderDao transferOrderDao;

	@Before
	public void beforeTest() {
		transferService = Guice.createInjector(new AppTestModule())
				.getInstance(TransferService.class);
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteTransferError() {
		// Given
		// Nothing

		// When
		transferService.deleteTransfer(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testDeleteTransferNegative() {
		// Given
		final long orderId = 1L;
		doReturn(false).when(transferOrderDao).contains(eq(orderId));

		// When
		transferService.deleteTransfer(orderId);

		// Then
		verify(transferOrderDao, never()).delete(orderId);
	}

	@Test
	public void testDeleteTransferPositive() {
		// Given
		final long orderId = 1L;
		doReturn(true).when(transferOrderDao).contains(eq(orderId));

		// When
		transferService.deleteTransfer(orderId);

		// Then
		verify(transferOrderDao).delete(orderId);
	}

	@Test(expected = NullPointerException.class)
	public void testDoesTransferExistError() {
		// Given
		// Nothing

		// When
		transferService.doesTransferExist(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testDoesTransferExistNegative() {
		// Given
		final long orderId = 1L;
		doReturn(false).when(transferOrderDao).contains(eq(orderId));

		// When
		final boolean result = transferService.doesTransferExist(orderId);

		// Then
		assertFalse(result);
	}

	@Test
	public void testDoesTransferExistPositive() {
		// Given
		final long orderId = 1L;
		doReturn(true).when(transferOrderDao).contains(eq(orderId));

		// When
		final boolean result = transferService.doesTransferExist(orderId);

		// Then
		assertTrue(result);
	}

	@Test(expected = NullPointerException.class)
	public void testGetTransferError() {
		// Given
		// Nothing

		// When
		transferService.getTransfer(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testGetTransferNegative() {
		// Given
		final long orderId = 1L;
		doReturn(Optional.empty()).when(transferOrderDao).get(any(Long.class));

		// When
		final var result = transferService.getTransfer(orderId);

		// Then
		assertTrue(result.isEmpty());
	}

	@Test
	public void testGetTransferPositive() {
		// Given
		final long orderId = 1L;
		final var order = buildOrder1(orderId);
		doReturn(Optional.of(order)).when(transferOrderDao).get(eq(orderId));

		// When
		final var result = transferService.getTransfer(orderId);

		// Then
		assertEquals(order, result.orElseThrow());
	}

	@Test
	public void testGetTransfers() {
		// Given
		final var order1 = buildOrder1(1L);
		final var order2 = buildOrder1(Long.MAX_VALUE);
		final var orders = Set.of(order1, order2);
		doReturn(orders).when(transferOrderDao).getAll();

		// When
		final var result = transferService.getTransfers();

		// Then
		assertEquals(orders, result);
	}

	@Test
	public void testNewTransferConflictingIdProvided() {
		// Given
		final var orderId = 5L;
		final var order = buildOrder1(orderId);
		final var persisted = buildOrder1(orderId + 1);
		doReturn(true).when(transferOrderDao).contains(orderId);
		doReturn(orderId + 1).when(transferOrderDao).generateId();

		// When
		final var result = transferService.newTransfer(order);

		// Then
		verify(transferOrderDao).persist(eq(persisted));
		assertEquals(persisted, result);
	}

	@Test(expected = NullPointerException.class)
	public void testNewTransferError() {
		// Given
		// Nothing

		// When
		transferService.newTransfer(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testNewTransferIdProvided() {
		// Given
		final var order = buildOrder1(5L);
		final var persisted = buildOrder1(5L);

		// When
		final var result = transferService.newTransfer(order);

		// Then
		verify(transferOrderDao).persist(eq(persisted));
		assertEquals(persisted, result);
	}

	@Test
	public void testNewTransferNullId() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(null);
		final var persisted = buildOrder1(orderId);
		doReturn(orderId).when(transferOrderDao).generateId();

		// When
		final var result = transferService.newTransfer(order);

		// Then
		verify(transferOrderDao).persist(eq(persisted));
		assertEquals(persisted, result);
	}

	@Test
	public void testUpdateTransferDifferentId() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(orderId);
		final var updatedOrder = buildOrder1(orderId + 1);

		// When
		final var result = transferService.updateTransfer(orderId + 1, order);

		// Then
		verify(transferOrderDao).persist(eq(updatedOrder));
		assertEquals(updatedOrder, result);
	}

	@Test(expected = NullPointerException.class)
	public void testUpdateTransferErrorId() {
		// Given
		// Nothing

		// When
		transferService.updateTransfer(null, buildOrder1(1L));

		// Then
		// Exception is thrown
	}

	@Test(expected = NullPointerException.class)
	public void testUpdateTransferErrorORder() {
		// Given
		// Nothing

		// When
		transferService.updateTransfer(1L, null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testUpdateTransferNullOrderId() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(null);
		final var updatedOrder = buildOrder1(orderId);

		// When
		final var result = transferService.updateTransfer(orderId, order);

		// Then
		verify(transferOrderDao).persist(eq(updatedOrder));
		assertEquals(updatedOrder, result);
	}

	@Test
	public void testUpdateTransferPartiallyCreateNew() {
		// Given
		final var orderId = 1L;
		final var order = buildOrder1(orderId);
		doReturn(Optional.empty()).when(transferOrderDao).get(eq(orderId));

		// When
		final var result = transferService.updateTransferPartially(orderId, order);

		// Then
		verify(transferOrderDao).persist(eq(order));
		assertEquals(order, result);
	}

	@Test
	public void testUpdateTransferPartiallyOnlyPart() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(orderId);
		final var patchOrder = buildOrder3(null);
		final var expectedOrder = TransferOrder.builder()
				.id(orderId)
				.sourceAccount(order.getSourceAccount())
				.targetAccount(order.getTargetAccount())
				.amount(patchOrder.getAmount())
				.status(patchOrder.getStatus())
				.build();

		doReturn(Optional.of(order)).when(transferOrderDao).get(eq(orderId));

		// When
		final var result = transferService.updateTransferPartially(orderId, patchOrder);

		// Then
		verify(transferOrderDao).persist(any(TransferOrder.class));
		assertEquals(expectedOrder, result);
	}

	@Test
	public void testUpdateTransferPartiallyPositive() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(orderId);
		final var patchOrder = buildOrder2(null);
		final var updatedOrder = buildOrder2(orderId);
		doReturn(Optional.of(order)).when(transferOrderDao).get(eq(orderId));

		// When
		final var result = transferService.updateTransferPartially(orderId, patchOrder);

		// Then
		verify(transferOrderDao).persist(eq(updatedOrder));
		assertEquals(updatedOrder, result);
	}

	@Test
	public void testUpdateTransferPositive() {
		// Given
		final var orderId = 3L;
		final var order = buildOrder1(orderId);

		// When
		final var result = transferService.updateTransfer(orderId, order);

		// Then
		verify(transferOrderDao).persist(eq(order));
		assertEquals(order, result);
	}

	private TransferOrder buildOrder1(final Long orderId) {
		return TransferOrder.builder()
				.id(orderId)
				.amount(BigDecimal.TEN)
				.sourceAccount("123")
				.targetAccount("456")
				.status(TransferStatus.PENDIG_RECEPTION)
				.build();
	}

	private TransferOrder buildOrder2(final Long orderId) {
		return TransferOrder.builder()
				.id(orderId)
				.amount(BigDecimal.ONE)
				.sourceAccount("000")
				.targetAccount("111")
				.status(TransferStatus.FINISHED)
				.build();
	}

	private TransferOrder buildOrder3(final Long orderId) {
		return TransferOrder.builder()
				.id(orderId)
				.amount(BigDecimal.ZERO)
				.status(TransferStatus.REJECTED)
				.build();
	}
}
