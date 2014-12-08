package com.zenika.formation.osgi.service.consumer.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

public class ServiceConsumer implements BundleActivator, ServiceListener {
	
	/**
	 * OSGi BundleContext.
	 */
	private BundleContext bundleContext = null;

	/**
	 * LogService filter.
	 */
	private final static String LOG_SERVICE_FILTER = "(&(" + Constants.OBJECTCLASS + "=" + LogService.class.getName() + ")(nature=basic))";

	/**
	 * HttpService filter.
	 */
	private final static String HTTP_SERVICE_FILTER = "(" + Constants.OBJECTCLASS + "=" + HttpService.class.getName() + ")";
	
	/**
	 * Service filter.
	 */
	private final static String FILTER = "(|" + LOG_SERVICE_FILTER + HTTP_SERVICE_FILTER + ")";

    /**
     * OSGi LogService.
     */
    private LogService logService;
    
    /**
     * OSGi HttpService.
     */
    private HttpService httpService;
    
    /**
     * OSGi BundleTracker.
     */
    private BundleTracker<List<String>> bundleTracker;
    
    /**
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
    	this.bundleContext = bundleContext;
    	
		bundleTracker = new BundleTracker<List<String>>(bundleContext, Bundle.ACTIVE, null) {
			public List<String> addingBundle(Bundle bundle, BundleEvent event) {
				// Registering servlets for this bundle if any
				return registerServlets(bundle);
			}
			public void removedBundle(Bundle bundle, BundleEvent event, List<String> object) {
				// Unregistering tracked aliases
				unregisterAliases(object);
			}
		};

    	// Getting the filtered service references
		ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences((String) null, FILTER);
        if (serviceReferences != null) {
        	for (ServiceReference<?> serviceReference : serviceReferences) {
	    		Object service = bundleContext.getService(serviceReference);
	    		if (service instanceof LogService) {
	    			bindLogService((LogService) service);
	        	} else {
	        		bindHttpService((HttpService) service);
	        	}    	
	    	}
        }
 

        // Adding OSGi listeners
        bundleContext.addServiceListener(this, FILTER);
    }

    /**
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
    	bundleTracker.close();
    	
    	// Removing OSGi listener
        bundleContext.removeServiceListener(this);
    }

    /**
     * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
     */
    public void serviceChanged(ServiceEvent event) {
    	Object service = bundleContext.getService(event.getServiceReference());
    	
        switch (event.getType()) {
        case ServiceEvent.REGISTERED:
        	if (service instanceof LogService) {
        		bindLogService((LogService) service);
        	} else {
        		bindHttpService((HttpService) service);
        	}
        	
            break;
        case ServiceEvent.UNREGISTERING:
        	if (service instanceof LogService) {
        		unbindLogService((LogService) service);
        	} else {
        		unbindHttpService((HttpService) service);
        	}
        	break;           
        }
    }
	
	/**
	 * Method called when the LogService is registered.
	 * @param logService LogService instance
	 */
	public void bindLogService(LogService logService) {
		this.logService = logService;
		
    	// Getting the bundle ID to log
    	long bundleId = bundleContext.getBundle().getBundleId();
    	log(LogService.LOG_INFO, "Message from bundle [" + bundleId + "]", null);
	}
	
	/**
	 * Method called before the LogService is unregistered.
	 * @param logService LogService instance
	 */
	public void unbindLogService(LogService logService) {
		this.logService = null;
	}
	
	/**
	 * Method called when the HttpService is registered.
	 * @param httpService HttpService instance
	 */
	public void bindHttpService(HttpService httpService) {
		this.httpService = httpService;
		
		// Starts tracking servlets
		bundleTracker.open();
	}
	
	/**
	 * Method called before the HttpService is unregistered.
	 * @param httpService HttpService instance
	 */
	public void unbindHttpService(HttpService httpService) {
		// Stops tracking servlets
		bundleTracker.close();
		
		this.httpService = null;
	}
	
	/**
	 * Logs a message using the LogService.
	 * @param level The log level
	 * @param message The log message
	 * @param exception The log exception
	 */
	public void log(int level, String message, Exception exception) {
        if (this.logService == null) {
            System.out.println("LogService not registered");
        } else {
        	this.logService.log(level, message, exception);
        }
	}
	
	/**
	 * Registers servlets if any.
	 * @param bundle Bundle to analyze
	 */
	private List<String> registerServlets(Bundle bundle) {
		List<String> aliases = new LinkedList<String>();
		Map<String, String> servletMap = getServletHeader(bundle);
		for (String alias : servletMap.keySet()) {
			String className = servletMap.get(alias);
			try {
				registerServlet(bundle, alias, className);
				aliases.add(alias);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		return aliases;
	}
	
	/**
	 * Registers a servlet into the URI namespace.
	 * @param bundle Bundle used to load the servlet class
	 * @param alias Name in the URI namespace at which the servlet is registered 
	 * @param className Servlet class name.
	 * @throws Exception If the registration fails 
	 */
	private void registerServlet(Bundle bundle, String alias, String className) throws Exception {
		Class<?> clazz = bundle.loadClass(className);
		if (clazz != null) {
			// Servlet creation and registration
			Servlet servlet = (Servlet) clazz.newInstance();
			log(LogService.LOG_INFO, "Registering servlet with alias: " + alias, null);
			httpService.registerServlet(alias, servlet, null, null);
		}
	}
	
	/**
	 * Unregisters aliases.
	 * @param bundle List of aliases to unregister
	 */
	private void unregisterAliases(List<String> aliases) {
		for (String alias : aliases) {
			unregisterAlias(alias);
		}
	}
	
	/**
	 * Unregisters alias.
	 * @param bundle Alias to unregister
	 */
	private void unregisterAlias(String alias) {
		log(LogService.LOG_INFO, "Unregistering servlet with alias " + alias, null);
		httpService.unregister(alias);
	}
	
	/**
	 * Gets the couple (key,value) of the Servlet-Map MANIFEST header
	 * @param bundle Bundle to analyze
	 * @return The couple (key,value) of the Servlet-Map MANIFEST header
	 */
	private Map<String, String> getServletHeader(Bundle bundle) {
		Map<String, String> servletMap = new HashMap<String, String>();
		String servletHeader = (String) bundle.getHeaders().get("Servlet-Map");
		if (servletHeader != null) {
			String clauses[] = servletHeader.split(",");
	        for (String clause : clauses) {
	          String parts[] = clause.trim().split("=");
	          if (parts.length == 2) {
	        	  servletMap.put(parts[0], parts[1]);
	          }
	        }
		}
		return servletMap;
	}
}
