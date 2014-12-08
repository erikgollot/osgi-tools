package com.zenika.formation.osgi.service.consumer.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Generic Servlet implementation.
 * @author François Fornaciari
 */
public class GenericServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private String text;
	
	/**
	 * Reference to the EventAdmin Service.
	 */
	private EventAdmin eventAdmin;

	/**
	 * Event topic name.
	 */
	private static final String PAGE_VISITED = "PAGE_VISITED";
	
	public GenericServlet(String text) {
		this.text = text;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String title = "Zenika - Formation OSGi";
		String top = "Generic Servlet";
		
		response.getWriter().print("<html><head><title>");
		response.getWriter().print(title);
		response.getWriter().print("</title></head><body style=\"-moz-box-shadow: 5px 5px 18px #000000;-webkit-box-shadow: 5px 5px 18px #000000;box-shadow: 5px 5px 18px #000000;padding: 10px;margin: 20px\">");
		response.getWriter().print("<h1 style=\"background-color: #E5E3DF; padding: 5px\">" + top + "</h1>");
		response.getWriter().print("<div style=\"padding: 5px;\"><b>Text:</b> " + text + "</div>");
		response.getWriter().print("</body></html>");
		
		sendPageVisitedEvent(request.getRequestURI());
	}

	/**
	 * Send an asynchronous event each time this page is visited.
	 * @param requestURI Alias of the visited page.
	 */
	private void sendPageVisitedEvent(String requestURI) {
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put("requestURI", requestURI);
		Event event = new Event(PAGE_VISITED, properties);
		eventAdmin.postEvent(event);
	}

	public EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
}
