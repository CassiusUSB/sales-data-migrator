package io.casswolfe.persistenceconfig;

import io.casswolfe.Configuration;
import io.casswolfe.exception.SqlConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.url = Configuration.getenv("app.db-url");
        this.user = Configuration.getenv("app.db-user");
        this.password = Configuration.getenv("app.db-password");
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
