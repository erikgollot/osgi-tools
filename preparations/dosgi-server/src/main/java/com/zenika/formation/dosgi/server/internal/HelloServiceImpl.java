package com.zenika.formation.dosgi.server.internal;

import com.zenika.formation.dosgi.service.HelloService;

/**
 * @{HelloService} service implementation.
 * @author François Fornaciari
 */
public class HelloServiceImpl implements HelloService {
	
	/**
	 * @see com.zenika.formation.dosgi.service.HelloService#sayHello(java.lang.String)
	 */
	public String sayHello(String name) {
        return "Hello " + name;
    }
}
