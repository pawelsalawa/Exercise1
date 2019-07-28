package pl.com.salsoft.exercise1.rest;

import java.util.Optional;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import pl.com.salsoft.exercise1.service.JsonService;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Foundation for all REST controllers.
 * It provides common support for error handling and typical execution flow.
 */
public abstract class AbstractController {
	private static final String APPLICATION_JSON = "application/json";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	private JsonService jsonService;

	/**
	 * It's a proxy method for exception handling for all REST calls on this controller.
	 * In case of exception in the target handler, HTTP status 500 is returned and empty body in response.
	 * Also an appropriate error is logged with the request and the error message.
	 * In case of successful processing, a response object (if provided) is serialized to JSON format.
	 * If response object was not provided, empty body is returned to the caller.
	 * @param actionHandler Target handler that does actual job.
	 * @return Result from the target handler, or null if any exception was thrown.
	 */
	protected Route handle(final Route actionHandler) {
		return (final Request request, final Response response) -> {
			try {
				response.type(APPLICATION_JSON);
				final var body = Optional.ofNullable(actionHandler.handle(request, response));
				return body.map(jsonService::map).orElse("");
			} catch (final Exception e) {
				log.error("Error while handling request {} {}: {}", request.requestMethod(), request.url(), e.getMessage());
				response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
				return "";
			}
		};
	}
}
