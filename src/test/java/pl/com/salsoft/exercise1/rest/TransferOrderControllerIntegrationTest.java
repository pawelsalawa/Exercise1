package pl.com.salsoft.exercise1.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;

import pl.com.salsoft.exercise1.AppModule;
import pl.com.salsoft.exercise1.model.TransferOrder;
import pl.com.salsoft.exercise1.model.TransferStatus;
import spark.Spark;

public class TransferOrderControllerIntegrationTest {

	private static final String URL_PATTERN = "http://localhost:%d/%s";
	private static final String APPLICATION_JSON = "application/json";
	private static final int FREE_PORT = findFreePort();
	private static final ObjectMapper mapper = new ObjectMapper();

	private static int findFreePort() {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpClient client;

	@After
	public void afterTest() {
		Spark.stop();
		Spark.awaitStop();
	}

	@Before
	public void beforeTest() {
		Spark.port(FREE_PORT);
		client = HttpClient.newHttpClient();

		Guice.createInjector(new AppModule())
				.getInstance(TransferOrderController.class)
				.initMapping();
	}

	@Test
	public void testCreateConflictingAndGetLast() throws IOException, InterruptedException {
		// Given
		final var order1 = buildOrder1(1L);
		final var order2 = buildOrder2(null);
		final var expectedResponse1 = buildOrder1(1L);
		final var expectedResponse2 = buildOrder2(2L);

		// When
		final var responsePost1 = post("transfer", toJson(order1));
		final var responsePost2 = post("transfer", toJson(order2));
		final var responseGet = get("transfer/2");

		// Then
		assertEquals(HttpStatus.CREATED_201, responsePost1.statusCode());
		assertEquals(toJson(expectedResponse1), responsePost1.body());
		assertEquals(HttpStatus.CREATED_201, responsePost2.statusCode());
		assertEquals(toJson(expectedResponse2), responsePost2.body());

		final var responseOrder = fromJson(responseGet.body(), TransferOrder.class);
		assertEquals(HttpStatus.OK_200, responseGet.statusCode());
		assertEquals(responseOrder, expectedResponse2);
	}

	@Test
	public void testCreateTwoAndDeleteFirst() throws IOException, InterruptedException {
		// Given
		final var order1 = buildOrder1(null);
		final var order2 = buildOrder1(null);
		final var expectedResponse2 = buildOrder1(1L);

		// When
		post("transfer", toJson(order1));
		post("transfer", toJson(order2));
		final var responseDelete = delete("transfer/0");
		final var responseRetryDelete = delete("transfer/0");
		final var responseGet1 = get("transfer/0");
		final var responseGet2 = get("transfer/1");

		// Then
		assertEquals(HttpStatus.NO_CONTENT_204, responseDelete.statusCode());
		assertEquals("", responseDelete.body());
		assertEquals(HttpStatus.NOT_FOUND_404, responseRetryDelete.statusCode());
		assertEquals("", responseRetryDelete.body());
		assertEquals(HttpStatus.NOT_FOUND_404, responseGet1.statusCode());
		assertEquals("", responseGet1.body());
		assertEquals(HttpStatus.OK_200, responseGet2.statusCode());
		assertEquals(toJson(expectedResponse2), responseGet2.body());
	}

	@Test
	public void testCreateTwoAndGetAll() throws IOException, InterruptedException {
		// Given
		final var order1 = buildOrder1(null);
		final var order2 = buildOrder1(null);
		final var expectedResponse1 = buildOrder1(0L);
		final var expectedResponse2 = buildOrder1(1L);
		final var expectedOrders = List.of(expectedResponse1, expectedResponse2);

		// When
		final var responsePost1 = post("transfer", toJson(order1));
		final var responsePost2 = post("transfer", toJson(order2));
		final var responseGet = get("transfer");

		// Then
		assertEquals(HttpStatus.CREATED_201, responsePost1.statusCode());
		assertEquals(toJson(expectedResponse1), responsePost1.body());
		assertEquals(HttpStatus.CREATED_201, responsePost2.statusCode());
		assertEquals(toJson(expectedResponse2), responsePost2.body());

		final List<TransferOrder> responseOrders = fromJson(responseGet.body(), new TypeReference<List<TransferOrder>>() {});
		responseOrders.sort(Comparator.comparing(TransferOrder::getId));
		assertEquals(HttpStatus.OK_200, responseGet.statusCode());
		assertEquals(expectedOrders, responseOrders);
	}

	@Test
	public void testCreateTwoAndUpdateFirst() throws IOException, InterruptedException {
		// Given
		final var order1 = buildOrder1(null);
		final var order2 = buildOrder1(null);
		final var updateOrder = buildOrder3(0L);

		// When
		post("transfer", toJson(order1));
		post("transfer", toJson(order2));
		final var responsePut = put("transfer/0", toJson(updateOrder));
		final var responseGet = get("transfer/0");

		// Then
		assertEquals(HttpStatus.OK_200, responsePut.statusCode());
		assertEquals(toJson(updateOrder), responsePut.body());
		assertEquals(HttpStatus.OK_200, responseGet.statusCode());
		assertEquals(toJson(updateOrder), responseGet.body());
	}

	@Test
	public void testCreateTwoAndUpdateFirstPartially() throws IOException, InterruptedException {
		// Given
		final var order1 = buildOrder1(null);
		final var order2 = buildOrder1(null);
		final var patchOrder1 = buildOrder3(0L);
		final var expectedOrder = TransferOrder.builder()
				.id(0L)
				.sourceAccount(order1.getSourceAccount())
				.targetAccount(order1.getTargetAccount())
				.amount(patchOrder1.getAmount())
				.status(patchOrder1.getStatus())
				.build();

		// When
		post("transfer", toJson(order1));
		post("transfer", toJson(order2));
		final var responsePatch = patch("transfer/0", toJson(patchOrder1));
		final var responseGet = get("transfer/0");

		// Then
		assertEquals(HttpStatus.OK_200, responsePatch.statusCode());
		assertEquals(toJson(expectedOrder), responsePatch.body());
		assertEquals(HttpStatus.OK_200, responseGet.statusCode());
		assertEquals(toJson(expectedOrder), responseGet.body());
	}

	@Test
	public void testGetEmpty() throws IOException, InterruptedException {
		// Given
		final Set<TransferOrder> orders = Set.of();
		final String json = toJson(orders);

		// When
		final var response = get("transfer");

		// Then
		assertEquals(HttpStatus.OK_200, response.statusCode());
		assertEquals(json, response.body());
	}

	@Test
	public void testHead() throws IOException, InterruptedException {
		// Given

		// When
		final var response = head("transfer");

		// Then
		assertEquals(HttpStatus.OK_200, response.statusCode());
		assertEquals("", response.body());
	}

	@Test
	public void testOptions() throws IOException, InterruptedException {
		// Given
		final Set<String> expectedActions = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");

		// When
		final var response = options("transfer");

		// Then
		assertEquals(HttpStatus.OK_200, response.statusCode());
		assertEquals("", response.body());

		final Map<String, List<String>> headers = response.headers().map();
		assertTrue(headers.containsKey("Allow"));
		assertNotNull(headers.get("Allow"));
		assertEquals(1, headers.get("Allow").size());

		final String allow = headers.get("Allow").get(0).toString();
		final Set<String> responseActions = Pattern.compile("\\s*,\\s*").splitAsStream(allow).collect(Collectors.toSet());
		assertEquals(expectedActions, responseActions);
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

	private HttpResponse<String> delete(final String resource) throws IOException, InterruptedException {
		return send(builder -> builder.DELETE(), resource);
	}

	private <T> T fromJson(final String json, final Class<T> type) throws IOException {
		return mapper.readValue(json, type);
	}

	private <T> T fromJson(final String json, final TypeReference<T> type) throws IOException {
		return mapper.readValue(json, type);
	}

	private HttpResponse<String> get(final String resource) throws IOException, InterruptedException {
		return send(builder -> builder.GET(), resource);
	}

	private HttpResponse<String> head(final String resource) throws IOException, InterruptedException {
		return send(builder -> builder.method("HEAD", BodyPublishers.noBody()), resource);
	}

	private HttpResponse<String> options(final String resource) throws IOException, InterruptedException {
		return send(builder -> builder.method("OPTIONS", BodyPublishers.noBody()), resource);
	}

	private HttpResponse<String> patch(final String resource, final String body) throws IOException, InterruptedException {
		return send(builder -> builder.method("PATCH", BodyPublishers.ofString(body)), resource);
	}

	private HttpResponse<String> post(final String resource, final String body) throws IOException, InterruptedException {
		return send(builder -> builder.POST(BodyPublishers.ofString(body)), resource);
	}

	private HttpResponse<String> put(final String resource, final String body) throws IOException, InterruptedException {
		return send(builder -> builder.PUT(BodyPublishers.ofString(body)), resource);
	}

	private HttpResponse<String> send(final Consumer<Builder> methodProvider, final String resource) throws IOException, InterruptedException {
		final Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(String.format(URL_PATTERN, FREE_PORT, resource)))
				.header("Content-Type",  APPLICATION_JSON)
				.version(Version.HTTP_2);

		methodProvider.accept(builder);
		final HttpRequest request = builder.build();
		return client.send(request, BodyHandlers.ofString());
	}

	private String toJson(final Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
}
