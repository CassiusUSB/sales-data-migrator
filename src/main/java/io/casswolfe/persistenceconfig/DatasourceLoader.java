package io.casswolfe.persistenceconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatasourceLoader {
    private static final Logger LOG = LoggerFactory.getLogger("DatasourceLoader.class");

    public Connection getS(Properties applicationProperties){

        try {
            Properties dbProps = new Properties();
            dbProps.setProperty("user", applicationProperties.getProperty("app.db-user"));
            dbProps.setProperty("password", applicationProperties.getProperty("app.db-password"));
            dbProps.setProperty("ssl", "false");

            return DriverManager.getConnection(applicationProperties.getProperty("app.db-url"), dbProps);
        } catch (SQLException e) {
            LOG.error("Failed to connect to db. {}", e.toString());
            throw new RuntimeException(e);
        }
    }
}
