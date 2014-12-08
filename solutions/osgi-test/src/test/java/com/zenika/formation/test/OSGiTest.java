package com.zenika.formation.test;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.sql.Driver;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;

/**
 * OSGi tests.
 * @author François Fornaciari
 */

@RunWith(PaxExam.class)
public class OSGiTest {

	@Inject
	private BundleContext bundleContext;
	
	@Inject
	private DataSourceFactory dataSourceFactory;

	@Configuration
	public Option[] configuration() {
		return options(
			// Provisioning bundles
			mavenBundle("com.zenika.formation.osgi", "logservice-provider", "0.1.0-SNAPSHOT"),
			mavenBundle("org.osgi", "org.osgi.compendium", "4.3.1").noStart(),
			wrappedBundle(mavenBundle("javax.servlet", "servlet-api", "2.5")).noStart(),
			mavenBundle("com.zenika.formation.osgi", "jdbc-provider", "0.1.0-SNAPSHOT"),
			junitBundles(),
			vmOption("-Xmx512m")
		);
	}

	/**
	 * Verify that the BundleContext is available.
	 */
	@Test
	public void testBundleContextAvailability() {
		Assert.assertNotNull(bundleContext);
	}

	/**
	 * Verify that the LogService bundle is correctly installed.
	 */
	@Test
	public void testLogServiceProviderBundleIsInstalled() {
		boolean isLogServiceProviderInstalled = false;
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(
					"com.zenika.formation.osgi.logservice-provider")) {
				isLogServiceProviderInstalled = true;
			}
		}
		Assert.assertTrue(isLogServiceProviderInstalled);
	}
	
	/**
	 * Verify that the LogService bundle is correctly installed.
	 */
	@Test
	public void testLogServiceProviderBundleIsActive() {
		boolean isLogServiceProviderActive = false;
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(
					"com.zenika.formation.osgi.logservice-provider")) {
				if (bundle.getState() == Bundle.ACTIVE) {
					isLogServiceProviderActive = true;
				}
			}
		}
		Assert.assertTrue(isLogServiceProviderActive);
	}

	/**
	 * Verify that a LogService is correctly registered.
	 */
	@Test
	public void testLogServiceIsRegistered() {
		ServiceReference<LogService> serviceReference = bundleContext.getServiceReference(LogService.class);
		LogService logService = bundleContext.getService(serviceReference);
		Assert.assertNotNull(logService);
	}
	
	/**
	 * Verify that the Driver version is equal to 1.3 (by injection).
	 * @throws SQLException 
	 */
	@Test
	public void testSQLDriverVersionAvailability() throws SQLException {
		Driver driver = dataSourceFactory.createDriver(null);
		Assert.assertEquals(driver.getMajorVersion(), 1);
		Assert.assertEquals(driver.getMinorVersion(), 3);
	}
}
