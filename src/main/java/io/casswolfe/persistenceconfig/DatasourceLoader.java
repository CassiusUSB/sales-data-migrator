package io.casswolfe.persistenceconfig;

import io.casswolfe.exception.PropertiesFileNotFoundException;
import io.casswolfe.exception.SqlConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatasourceLoader {
    private static final Logger LOG = LoggerFactory.getLogger("DatasourceLoader.class");
    private final String url;
    private final String user;
    private final String password;

    public DatasourceLoader() {

        String profile = System.getenv("profile");
        Properties prop = new Properties();
        try {
            if ("EC2".equals(profile)) {
                prop.load(ClassLoader.getSystemResourceAsStream("EC2.properties"));
            } else {
                prop.load(ClassLoader.getSystemResourceAsStream("local.properties"));
            }
        } catch (IOException e) {
            throw new PropertiesFileNotFoundException(e);
        }

        this.url = prop.getProperty("app.db-url");
        this.user = prop.getProperty("app.db-user");
        this.password = prop.getProperty("app.db-password");
    }

    public Connection getConnection() {

        try {
            Properties dbProps = new Properties();
            dbProps.setProperty("user", user);
            dbProps.setProperty("password", password);
            dbProps.setProperty("ssl", "false");
            dbProps.setProperty("auto-commit", "false");

            Connection connection = DriverManager.getConnection(url, dbProps);
            String inspectDb = """
                    SELECT *
                    FROM pg_catalog.pg_tables
                    WHERE schemaname != 'pg_catalog' AND
                        schemaname != 'information_schema';
                    """;
            ResultSet resultSet = connection.createStatement().executeQuery(inspectDb);
            if (!resultSet.next()) {
                throw new SqlConnectionException("There are no tables in the database");
            }
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            LOG.error("Failed to connect to db. {}", e.toString());
            throw new SqlConnectionException(e);
        }
    }
}
