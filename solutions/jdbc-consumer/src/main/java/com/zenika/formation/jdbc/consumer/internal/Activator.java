package com.zenika.formation.jdbc.consumer.internal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Connect to the H2 Database through the DataSourceFactory service.
 * @author François Fornaciari
 */
public class Activator implements BundleActivator {
	
	/**
	 * DataSourceFactory service tracker.
	 */
	ServiceTracker<DataSourceFactory, DataSourceFactory> tracker = null;

	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext bundleContext) throws Exception {
		tracker = new ServiceTracker<DataSourceFactory, DataSourceFactory>(bundleContext, DataSourceFactory.class, null) {
			@Override
			public DataSourceFactory addingService(ServiceReference<DataSourceFactory> reference)  {
				DataSourceFactory dataSourceFactory = (DataSourceFactory) bundleContext.getService(reference);
				try {
					printDriverVersion(dataSourceFactory);
					printCurrentDate(dataSourceFactory);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return super.addingService(reference);
			}
			
			@Override
			public void removedService(ServiceReference<DataSourceFactory> reference, DataSourceFactory service) {
				super.removedService(reference, service);
				System.out.println("H2 Database stopping");			
			}
		};
		
		// Start tracking the DataSourceFactory service
		tracker.open();		
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext bundleContext) throws Exception {
		if (tracker != null) {
			// Stop tracking the DataSourceFactory service
			tracker.close();
		}
	}
	
	/**
	 * Print the H2 Driver version.
	 * @param dataSourceFactory Reference to DataSourceFactory service
	 * @throws SQLException If the operation fails
	 */
	private void printDriverVersion(DataSourceFactory dataSourceFactory) throws SQLException {
		Driver driver = dataSourceFactory.createDriver(null);
		System.out.println("H2 Database " + driver.getMajorVersion() + "." + driver.getMinorVersion() + " started"); 
	}
	
	/**
	 * Print the current SQL date.
	 * @param dataSourceFactory Reference to DataSourceFactory service
	 * @throws SQLException If the operation fails
	 */
	private void printCurrentDate(DataSourceFactory dataSourceFactory) throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			Properties properties = new Properties();
			properties.put(DataSourceFactory.JDBC_URL, "jdbc:h2:~/test");
			properties.put(DataSourceFactory.JDBC_USER, "sa");
			properties.put(DataSourceFactory.JDBC_PASSWORD, "");
			
		    DataSource dataSource = dataSourceFactory.createDataSource(properties);
		    
		    connection = dataSource.getConnection();
		    statement = connection.createStatement();
		    
		    resultSet = statement.executeQuery("select CURRENT_DATE()");
		    
		    while (resultSet.next()) {
		    	System.out.println("Current date : " + resultSet.getDate(1));
		    }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
	}

}
