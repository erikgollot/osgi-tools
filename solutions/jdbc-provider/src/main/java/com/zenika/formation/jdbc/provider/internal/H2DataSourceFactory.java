package com.zenika.formation.jdbc.provider.internal;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Implementation of the DataSourceFactory service.
 * @author François Fornaciari
 */
public class H2DataSourceFactory implements DataSourceFactory {

	private JdbcDataSource createJdbcDataSource(Properties properties) {
		JdbcDataSource jdbcDataSource = new JdbcDataSource();
		jdbcDataSource.setURL(properties.getProperty(JDBC_URL));
		jdbcDataSource.setUser(properties.getProperty(JDBC_USER));
		jdbcDataSource.setPassword(properties.getProperty(JDBC_PASSWORD));
		return jdbcDataSource;
	}

	public DataSource createDataSource(Properties properties) throws SQLException {
		return createJdbcDataSource(properties);
	}

	public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties) throws SQLException {
		return createJdbcDataSource(properties);
	}

	public XADataSource createXADataSource(Properties properties) throws SQLException {
		return createJdbcDataSource(properties);
	}

	public Driver createDriver(Properties props) throws SQLException {
		return new org.h2.Driver();
	}

}
