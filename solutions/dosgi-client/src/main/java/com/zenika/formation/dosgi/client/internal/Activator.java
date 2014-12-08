package com.zenika.formation.dosgi.client.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.zenika.formation.dosgi.service.HelloService;

/**
 * @author François Fornaciari
 */
public class Activator implements BundleActivator {

	/**
	 * HelloService tracker.
	 */
	private ServiceTracker<HelloService, HelloService> tracker;

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext bundleContext) throws Exception {
		tracker = new ServiceTracker<HelloService, HelloService>(bundleContext, HelloService.class, null) {
            public HelloService addingService(ServiceReference<HelloService> reference) {
            	HelloService result = super.addingService(reference);
                HelloService helloService = bundleContext.getService(reference);
                System.out.println(helloService.sayHello("Zenika"));
                return result;
            }
            public void remove(ServiceReference<HelloService> reference) {
            	System.out.println("removed");
            }
        };
        tracker.open();
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext bundleContext) throws Exception {
		if (tracker != null) {
			tracker.close();
		}
	}
}
