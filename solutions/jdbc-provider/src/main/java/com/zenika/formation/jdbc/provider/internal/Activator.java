package com.zenika.formation.jdbc.provider.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.h2.tools.Server;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Register the DataSourceFactory service for the H2 Database.
 * @author François Fornaciari
 */
public class Activator implements BundleActivator {
	
	/**
	 * H2 Server.
	 */
	private Server server = null;
	
	/**
	 * DataSourceFactory service registration.
	 */
	private ServiceRegistration<DataSourceFactory> resgistration;

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		server = Server.createWebServer().start();
		
		DataSourceFactory dataSourceFactory = new H2DataSourceFactory();
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver");
		properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "H2");
		properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, "4.0");
		
		resgistration = bundleContext.registerService(DataSourceFactory.class, dataSourceFactory, properties);
		
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (server != null) {
			server.stop();
		}
		
		if (resgistration != null) {
			resgistration.unregister();
		}
	}

}
