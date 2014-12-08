package com.zenika.formation.dosgi.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Hello REST service.
 * @author François Fornaciari
 */
@Path("/hello")
public interface HelloService {
	
	@GET
	@Path("{name}")
    String sayHello(@PathParam("name") String name);
}
