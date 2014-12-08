package com.zenika.formation.osgi.logservice.provider.internal;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * A basic implementation of the OSGi Log Service.
 * @author François Fornaciari
 */
@Component(name="BasicLogService")
@Provides
public class BasicLogService implements LogService {
	
	@ServiceProperty(value="basic")
	private String nature;

	public void log(int level, String message) {
		log(null, level, message, null);
	}

	public void log(int level, String message, Throwable exception) {
		log(null, level, message, exception);
	}

	public void log(ServiceReference serviceReference, int level, String message) {
		log(serviceReference, level, message, null);
	}

	public void log(ServiceReference serviceReference, int level, String message, Throwable exception) {
		String userLevel = null;

		if (exception != null) {
			message += ", exception:" + exception.getCause();
		}

		switch (level) {
		case LogService.LOG_ERROR:
			userLevel = "ERROR";
			break;
		case LogService.LOG_WARNING:
			userLevel = "WARNING";
			break;
		case LogService.LOG_INFO:
			userLevel = "INFO";
			break;
		case LogService.LOG_DEBUG:
			userLevel = "DEBUG";
			break;
		}

		System.out.println("[" + userLevel + "] " + message);
	}

}
