package pl.com.salsoft.exercise1;

import com.google.inject.Guice;

import pl.com.salsoft.exercise1.rest.TransferOrderController;
import spark.Spark;

/**
 * Exercise1 application entry point.
 */
public class App {

	/**
	 * Runs Spark (REST) and Guice (IoC/DI), effectively starting the application.
	 * @param args Command line arguments. None are supported at the moment. Anything passed here will be ignored.
	 * @throws InterruptedException
	 */
	public static void main(final String[] args) throws InterruptedException {
		Spark.port(8000);

		Guice.createInjector(new AppModule())
			.getInstance(TransferOrderController.class)
			.initMapping();
	}
}
