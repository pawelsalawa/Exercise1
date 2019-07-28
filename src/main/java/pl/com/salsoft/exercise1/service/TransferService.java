package pl.com.salsoft.exercise1.service;

import java.util.Optional;
import java.util.Set;

import com.google.inject.Inject;

import lombok.NonNull;
import pl.com.salsoft.exercise1.dao.TransferOrderDao;
import pl.com.salsoft.exercise1.model.TransferOrder;

/**
 * Service managing transfer orders. It serves typical CRUD operations.
 */
public class TransferService {
	@Inject
	private TransferOrderDao transferOrderDao;

	/**
	 * Deletes transfer order with given ID from the repository.
	 * @param id ID of order to delete. Cannot be null.
	 * @return true if order existed and was deleted, false otherwise.
	 */
	public boolean deleteTransfer(@NonNull final Long id) {
		if (!transferOrderDao.contains(id)) {
			return false;
		}

		transferOrderDao.delete(id);
		return true;
	}

	/**
	 * Checks whether transfer order with given ID exists in the repository.
	 * @param id ID of order to check. Cannot be null.
	 * @return true if the order exists, or false otherwise.
	 */
	public boolean doesTransferExist(@NonNull final Long id) {
		return transferOrderDao.contains(id);
	}

	/**
	 * Retrieves transfer order with given ID from the repository.
	 * @param id ID of order to get. Cannot be null.
	 * @return Requested order, or empty Optional if no order with given ID is stored in the repository.
	 */
	public Optional<TransferOrder> getTransfer(@NonNull final Long id) {
		return transferOrderDao.get(id);
	}

	/**
	 * Gets all transfer orders currently stored in the repository.
	 * @return A set of orders. Can be empty set, but never null.
	 */
	public Set<TransferOrder> getTransfers() {
		return transferOrderDao.getAll();
	}

	/**
	 * Creates new transfer order from the order object provided.
	 * If ID of order provided is null, then next available ID is automatically generated.
	 * If ID of order provided is already used by different order instance,
	 * then next available ID is automatically generated.
	 *
	 * @param order Order to create. Cannot be null.
	 * @return Created order.
	 */
	public TransferOrder newTransfer(@NonNull TransferOrder order) {
		if (order.getId() == null || transferOrderDao.contains(order.getId())) {
			order = order.toBuilder()
						.id(transferOrderDao.generateId())
						.build();
		}
		transferOrderDao.persist(order);
		return order;
	}

	/**
	 * Replaces existing resource of with ID with new order value.
	 * If resource with that ID does not exist, a new resource is created.
	 * ID member of the order is ignored and will be overwritten with the existingOrderId.
	 * @param existingOrderId ID of existing resource.
	 * @param order New order value to set for given ID.
	 * @return Updated order (different instance than the input order object).
	 */
	public TransferOrder updateTransfer(@NonNull final Long existingOrderId, @NonNull TransferOrder order) {
		// ID cannot be set from payload, it should always reflect requested ID
		order = order.toBuilder().id(existingOrderId).build();

		transferOrderDao.persist(order);
		return order;
	}

	/**
	 * Updates existing resource of with given ID, or creates new resource with values provided
	 * (if only few values of order were provided and rest are nulls, then new resource will have
	 * only these values provided filled in).
	 * ID member of the order is ignored and will be overwritten with the existingOrderId.
	 * @param existingOrderId ID of existing resource.
	 * @param order New order values to set for given ID. If any member of the order object is null,
	 * it will be skipped. Only non-null members are copied to an existing order.
	 * @return Updated order (different instance than the input order object).
	 */
	public TransferOrder updateTransferPartially(@NonNull final Long existingOrderId, @NonNull final TransferOrder order) {
		final TransferOrder mergedOrder = transferOrderDao.get(existingOrderId)
				// merge payload with existing order
				.map(existingOrder -> existingOrder.mergeNonNull(order))
				// or create new one from provided order
				.orElse(order.toBuilder()
						.id(existingOrderId) // ID cannot be set from payload, it should always reflect requested ID
						.build());

		transferOrderDao.persist(mergedOrder);
		return mergedOrder;
	}
}
