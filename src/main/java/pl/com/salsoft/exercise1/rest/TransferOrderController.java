package pl.com.salsoft.exercise1.rest;

import java.util.Optional;
import java.util.function.BiFunction;

import org.eclipse.jetty.http.HttpStatus;

import com.google.inject.Inject;

import lombok.NonNull;
import pl.com.salsoft.exercise1.model.TransferOrder;
import pl.com.salsoft.exercise1.service.JsonService;
import pl.com.salsoft.exercise1.service.TransferService;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * REST controller serving the /transfer resource operations.
 *
 * It support most of the actions possible. Missing actions are variations
 * of PUT/PATCH/DELETE, where no ID is specified, that is when these actions
 * are supposed to be executed on whole collection.
 */
public class TransferOrderController extends AbstractController {
	private static final String SUPPORTED_ACTIONS = "GET,POST,PUT,DELETE,PATCH,OPTIONS,HEAD";
	private static final String ID = ":id";
	private static final String RESOURCE_ROOT = "/transfer";
	private static final String RESOURCE_BY_ID = String.format("%s/%s", RESOURCE_ROOT, ID);

	@Inject
	private TransferService transferService;

	@Inject
	private JsonService jsonService;

	/**
	 * Sets up all REST request mappings.
	 * Should be called at the application start.
	 */
	public void initMapping() {
		Spark.get(RESOURCE_ROOT, handle(this::getAll));
		Spark.get(RESOURCE_BY_ID, handle(this::getSingle));
		Spark.post(RESOURCE_ROOT, handle(this::post));
		Spark.put(RESOURCE_BY_ID, handle(this::put));
		Spark.delete(RESOURCE_BY_ID, handle(this::delete));
		Spark.patch(RESOURCE_BY_ID, handle(this::patch));
		Spark.options(RESOURCE_ROOT, handle(this::options));
		Spark.head(RESOURCE_ROOT, handle(this::head));
	}

	/**
	 * Implements DELETE method from REST.
	 */
	private Object delete(final Request request, final Response response) {
		final boolean deleted = transferService.deleteTransfer(readId(request));
		response.status(deleted ? HttpStatus.NO_CONTENT_204 : HttpStatus.NOT_FOUND_404);
		return null;
	}

	/**
	 * Implements GET method from REST, variation without ID.
	 */
	private Object getAll(final Request request, final Response response) {
		response.status(HttpStatus.OK_200);
		return transferService.getTransfers();
	}

	/**
	 * Implements GET method from REST, variation with order ID provided in the path.
	 */
	private Object getSingle(final Request request, final Response response) {
		final var order = transferService.getTransfer(readId(request));
		if (order.isPresent()) {
			response.status(HttpStatus.OK_200);
			return order.orElseThrow();
		}
		response.status(HttpStatus.NOT_FOUND_404);
		return null;
	}

	/**
	 * Implements HEAD method from REST.
	 */
	private Object head(final Request request, final Response response) {
		response.status(HttpStatus.OK_200);
		return null;
	}

	/**
	 * Implements OPTIONS method from REST.
	 */
	private Object options(final Request request, final Response response) {
		response.header("Allow", SUPPORTED_ACTIONS);
		response.status(HttpStatus.OK_200);
		return null;
	}

	/**
	 * Implements PATCH method from REST.
	 */
	private Object patch(final Request request, final Response response) {
		return update(request, response, transferService::updateTransferPartially);
	}

	/**
	 * Implements POST method from REST.
	 */
	private Object post(final Request request, final Response response) {
		response.status(HttpStatus.CREATED_201);
		return transferService.newTransfer(readOrder(request));
	}

	/**
	 * Implements PUT method from REST.
	 */
	private Object put(final Request request, final Response response) {
		return update(request, response, transferService::updateTransfer);
	}

	private @NonNull Long readId(final Request request) {
		return Long.parseLong(request.params(ID));
	}

	private @NonNull TransferOrder readOrder(final Request request) {
		return Optional.ofNullable(jsonService.map(request.body(), TransferOrder.class)).orElseThrow();
	}

	private Object update(final Request request, final Response response, final BiFunction<Long, TransferOrder, TransferOrder> updateFunction) {
		final var requestedId = readId(request);
		final var existed = transferService.doesTransferExist(requestedId);
		final var order = updateFunction.apply(readId(request), readOrder(request));
		response.status(existed ? HttpStatus.OK_200 : HttpStatus.CREATED_201);
		return order;
	}
}
