package pl.com.salsoft.exercise1.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import lombok.NonNull;
import pl.com.salsoft.exercise1.model.TransferOrder;

/**
 * Persistence handler for transfer orders. It carries all of common operations.
 * Underneath it uses a Map (JDK's ConcurrentHashMap to provide efficient thread-safety),
 * because it's fast, sufficient for the requirements and goes along with KISS principle.
 */
public class TransferOrderDao {
	private final AtomicLong idSequence = new AtomicLong(0L);
	private final Map<Long, TransferOrder> orderStore = new ConcurrentHashMap<>();

	/**
	 * Checks whether repository contains order with given ID.
	 * @param id Order ID to check. Cannot be null.
	 * @return true if order exists, or false otherwise.
	 */
	public boolean contains(@NonNull final Long id) {
		return orderStore.containsKey(id);
	}

	/**
	 * Deletes order with given ID from the repository.
	 * If order with this ID did not exist, this method does nothing.
	 * @param id ID of order to delete. Cannot be null.
	 */
	public void delete(@NonNull final Long id) {
		orderStore.remove(id);
	}

	/**
	 * Generates next available order ID. It's guaranteed to be unused.
	 * @return Generated ID.
	 */
	public long generateId() {
		return idSequence.getAndIncrement();
	}

	/**
	 * Finds order with given ID in the repository and returns it.
	 * @param id ID of order to find. Cannot be null.
	 * @return Requested order or empty Optional.
	 */
	public Optional<TransferOrder> get(@NonNull final Long id) {
		return Optional.ofNullable(orderStore.get(id));
	}

	/**
	 * Finds all orders in the repository and returns them.
	 * @return Unmodifiable set of orders. If no orders exist, then empty set is returned.
	 */
	public Set<TransferOrder> getAll() {
		final Collection<TransferOrder> values = orderStore.values();
		// While it's safe to call values() out of synchronized block,
		// iterating over the result collection should already be done inside of such block.
		// That's what Java documentation states.
		synchronized (orderStore) {
			return Set.copyOf(values);
		}
	}

	/**
	 * Saves given order into repository. If order with the same ID already existed,
	 * it will be replaced with the new one.
	 * @param order Order to store. Cannot be null. Also it must have ID defined.
	 * It's forbidden to call this method with order having null ID.
	 */
	public void persist(@NonNull final TransferOrder order) {
		orderStore.put(order.getId(), order);

		// If persisted order has bigger ID than current ID sequence, then we need to
		// traverse sequence (atomically) to value higher than this ID, so the #generateId()
		// returns unused ID.
		idSequence.updateAndGet(currentSequenceId -> {
			return order.getId() > currentSequenceId ? order.getId() + 1 : currentSequenceId;
		});
	}
}
