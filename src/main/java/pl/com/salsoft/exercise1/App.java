package pl.com.salsoft.exercise1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.inject.Guice;

import pl.com.salsoft.exercise1.rest.TransferOrderController;
import spark.Spark;

/**
 * Exercise1 application entry point.
 */
public class App {
	private static final String CONFIG_FILE_NAME = "config.properties";
	private static final String CONFIG_PORT = "port";
	private static final Properties CONFIG = new Properties();

	/**
	 * Runs Spark (REST) and Guice (IoC/DI), effectively starting the application.
	 * @param args Command line arguments. None are supported at the moment. Anything passed here will be ignored.
	 * @throws IOException If configuration file could not be read.
	 */
	public static void main(final String[] args) throws IOException {
		loadProperties();
		Spark.port(getServerPort());

		Guice.createInjector(new AppModule())
			.getInstance(TransferOrderController.class)
			.initMapping();
	}

	private static int getServerPort() {
		if (!CONFIG.containsKey(CONFIG_PORT)) {
			throw new RuntimeException(String.format("Missing configuration entry: %s", CONFIG_PORT));
		}
		return Integer.parseInt(CONFIG.get(CONFIG_PORT).toString());
	}

	private static void loadProperties() throws IOException {
		try (InputStream input = App.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
			CONFIG.load(input);
		}
	}
}
