package pl.com.salsoft.exercise1.model;

/**
 * Describes state of a transfer order.
 */
public enum TransferStatus {
	/**
	 * It was just created and is pending for processing.
	 */
	PLANNED,
	/**
	 * It was picked up by a processor and now is in progress.
	 */
	PROCESSING,
	/**
	 * Processing of this order is done on our side.
	 * It's now waiting to be picked up by the recipient system
	 * and that to be confirmed.
	 */
	PENDIG_RECEPTION,
	/**
	 * It was already processed and delivered to the recipient.
	 * No further work can be done on it.
	 */
	FINISHED,
	/**
	 * It was rejected by user or the system.
	 * The system doesn't support more details on the rejection at the moment.
	 */
	REJECTED,
	;
}
