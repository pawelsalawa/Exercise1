package pl.com.salsoft.exercise1;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import pl.com.salsoft.exercise1.dao.TransferOrderDao;
import pl.com.salsoft.exercise1.rest.TransferOrderController;
import pl.com.salsoft.exercise1.service.JsonService;
import pl.com.salsoft.exercise1.service.TransferService;

/**
 * Default IoC configuration for this application used by Guice framework.
 */
public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TransferOrderDao.class).in(Scopes.SINGLETON);
		bind(TransferOrderController.class).in(Scopes.SINGLETON);
		bind(TransferService.class).in(Scopes.SINGLETON);
		bind(JsonService.class).in(Scopes.SINGLETON);
	}

}
