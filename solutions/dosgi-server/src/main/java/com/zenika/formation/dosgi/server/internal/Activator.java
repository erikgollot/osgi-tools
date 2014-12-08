package com.zenika.formation.dosgi.server.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.zenika.formation.dosgi.service.HelloService;

/**
 * @author François Fornaciari
 */
public class Activator implements BundleActivator {
	
	/**
	 * HelloService registration.
	 */
	private ServiceRegistration<HelloService> registration;

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Dictionary<String, String> props = new Hashtable<String, String>();		
		props.put("service.exported.interfaces", "*");
		props.put("service.exported.configs", "org.apache.cxf.rs");
		props.put("org.apache.cxf.rs.address", "http://localhost:9090/dosgi");
		registration = bundleContext.registerService(HelloService.class, new HelloServiceImpl(), props);
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (registration != null) {
			registration.unregister();
		}
		
		
	}

}
