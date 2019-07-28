package pl.com.salsoft.exercise1.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import pl.com.salsoft.exercise1.model.TransferOrder;

public class TransferOrderDaoTest {
	private TransferOrderDao dao;

	@Before
	public void beforeTest() {
		dao = new TransferOrderDao();
	}

	@Test(expected = NullPointerException.class)
	public void testContainsError() {
		// Given
		// Nothing

		// When
		dao.contains(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testContainsNegative() {
		// Given
		final long orderId = 1L;
		dao.persist(buildOrder(orderId));

		// When
		final boolean result = dao.contains(orderId + 1);

		// Then
		assertFalse(result);
	}

	@Test
	public void testContainsPositive() {
		// Given
		final long orderId = 1L;
		dao.persist(buildOrder(orderId));

		// When
		final boolean result = dao.contains(orderId);

		// Then
		assertTrue(result);
	}

	@Test
	public void testDelete() {
		// Given
		final long orderId = 1L;
		dao.persist(buildOrder(orderId));

		// When
		dao.delete(orderId);

		// Then
		assertFalse(dao.contains(orderId));
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteError() {
		// Given
		// Nothing

		// When
		dao.delete(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testDeleteMultipleNoError() {
		// Given
		final long orderId = 1L;
		dao.persist(buildOrder(orderId));

		// When
		dao.delete(orderId);
		dao.delete(orderId);
		dao.delete(orderId + 1);

		// Then
		assertFalse(dao.contains(orderId));
	}

	@Test
	public void testGenerateIdAfterCustomPersist() {
		// Given
		final long orderId = 5L;
		dao.persist(buildOrder(orderId));

		// When
		final long result = dao.generateId();

		// Then
		assertEquals(orderId + 1, result);
	}

	@Test
	public void testGenerateIdFirstUse() {
		// Given
		// Nothing

		// When
		final long result = dao.generateId();

		// Then
		assertEquals(0L, result);
	}

	@Test
	public void testGenerateIdNthUse() {
		// Given
		dao.generateId();
		dao.generateId();

		// When
		final long result = dao.generateId();

		// Then
		assertEquals(2L, result);
	}

	@Test
	public void testGetAll() {
		// Given
		final Set<TransferOrder> orders = IntStream.range(0, 10)
			.boxed()
			.map(orderId -> buildOrder(orderId))
			.collect(Collectors.toSet());

		orders.forEach(order -> dao.persist(order));

		// When
		final var allOrders = dao.getAll();

		// Then
		assertEquals(orders, allOrders);
	}

	@Test
	public void testGetAllEmpty() {
		// Given
		// Nothing

		// When
		final var allOrders = dao.getAll();

		// Then
		assertTrue(allOrders.isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void testGetError() {
		// Given
		// Nothing

		// When
		dao.get(null);

		// Then
		// Exception is thrown
	}

	@Test
	public void testGetMultiple() {
		// Given
		final var orderId1 = 1L;
		final var order1 = buildOrder(orderId1);
		dao.persist(order1);

		final var orderId2 = Long.MAX_VALUE;
		final var order2 = buildOrder(orderId2);
		dao.persist(order2);

		// When
		dao.get(orderId1);
		dao.get(orderId2);
		final var result2 = dao.get(orderId2);

		// Then
		assertEquals(order2, result2.orElseThrow());
	}

	@Test
	public void testGetNegative() {
		// Given
		final var orderId = 1L;
		final var order = buildOrder(orderId);
		dao.persist(order);

		// When
		final var foundOrder = dao.get(orderId + 1);

		// Then
		assertFalse(foundOrder.isPresent());
	}

	@Test
	public void testGetPositive() {
		// Given
		final var orderId = 1L;
		final var order = buildOrder(orderId);
		dao.persist(order);

		// When
		final var foundOrder = dao.get(orderId);

		// Then
		assertEquals(order, foundOrder.orElseThrow());
	}

	@Test
	public void testPersistBigId() {
		// Given
		final var order = buildOrder(Long.MAX_VALUE);

		// When
		dao.persist(order);

		// Then
		// No exception thrown
	}

	@Test
	public void testPersistBigNegativeId() {
		// Given
		final var order = buildOrder(Long.MIN_VALUE);

		// When
		dao.persist(order);

		// Then
		// No exception thrown
	}

	@Test
	public void testPersistNoError() {
		// Given
		final var order = buildOrder(1L);

		// When
		dao.persist(order);

		// Then
		// No exception thrown
	}

	private TransferOrder buildOrder(final long orderId) {
		return TransferOrder.builder().id(orderId).build();
	}

}
