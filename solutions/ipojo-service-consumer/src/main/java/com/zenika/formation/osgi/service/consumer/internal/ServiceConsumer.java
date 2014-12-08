package com.zenika.formation.osgi.service.consumer.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

/**
 * ServiceConsumer.
 * @author François Fornaciari
 */
@Component(name="ServiceConsumer")
@Provides
public class ServiceConsumer implements ManagedServiceFactory {
	
	/**
	 * OSGi BundleContext.
	 */
	private BundleContext bundleContext;

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
    
	@ServiceProperty(name = Constants.SERVICE_PID, value = "com.zenika.formation.servlet.configuration")
	private String servicePid;
    
    /**
     * Association between a configuration PID and an alias.
     */
    private Map<String, String> genericServletPIDAliases = new HashMap<String, String>();
    
    /**
	 * EventAdmin Service.
	 */
    @Requires
    private EventAdmin eventAdmin;
    
    /**
     * Default constructor.
     * @param bundleContext OSGi BundleContext
     */
    public ServiceConsumer(BundleContext bundleContext) {
    	this.bundleContext = bundleContext;
    }

    /**
     * Start method called when all dependencies are registered.
     */
    @Validate
    public void start() {
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
		
		// Starts tracking servlets
		bundleTracker.open();
    }
    
    /**
     * Start method called when all dependencies are registered.
     */
    @Invalidate
    public void stop() {
    	// Unregisters generic servlets before stopping
		for (String alias : genericServletPIDAliases.values()) {
			unregisterAlias(alias);
		}
    	
    	// Stops tracking servlets
		bundleTracker.close();
    }
	
	/**
	 * Method called when the LogService is registered.
	 * @param logService LogService instance
	 */
	@Bind(filter="(nature=basic)")
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
	@Unbind
	public void unbindLogService(LogService logService) {
		this.logService = null;
	}
	
	/**
	 * Method called when the HttpService is registered.
	 * @param httpService HttpService instance
	 */
	@Bind
	public void bindHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	/**
	 * Method called before the HttpService is unregistered.
	 * @param httpService HttpService instance
	 */
	@Unbind
	public void unbindHttpService(HttpService httpService) {
    	// Stops tracking servlets
		bundleTracker.close();
		
		this.httpService = null;
	}
	
	public String getName() {
		return "ServletProvider";
	}

	/**
	 * Registers a Servlet for each detected configuration.
	 * Builds a new GenericServlet from the alias and the text of the configuration.
	 * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
	 */
	public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
		String alias = (String) properties.get("alias");
		String text = (String) properties.get("text");
		GenericServlet servlet = new GenericServlet(text);
		servlet.setEventAdmin(eventAdmin);
		try {
			if (genericServletPIDAliases.containsKey(pid)) {
				unregisterAlias(alias);
			}
			registerServlet(alias, servlet);
			genericServletPIDAliases.put(pid, alias);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public void deleted(String pid) {
		String alias = genericServletPIDAliases.get(pid);
		unregisterAlias(alias);
		genericServletPIDAliases.remove(pid);
	}
	
	/**
	 * Logs a message using the LogService.
	 * @param level The log level
	 * @param message The log message
	 * @param exception The log exception
	 */
	private void log(int level, String message, Exception exception) {
        if (this.logService == null) {
        	System.out.println(message);
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
			registerServlet(alias, servlet);
		}
	}
	
	/**
	 * Registers a servlet.
	 * @param alias Name in the URI namespace at which the servlet is registered 
	 * @param servlet Servlet to register
	 * @throws Exception If the registration fails 
	 */
	private void registerServlet(String alias, Servlet servlet) throws Exception {
		log(LogService.LOG_INFO, "Registering servlet with alias: " + alias, null);
		httpService.registerServlet(alias, servlet, null, null);
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
