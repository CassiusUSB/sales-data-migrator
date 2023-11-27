package io.casswolfe.service;

import io.casswolfe.exception.DataFileNotFoundException;
import io.casswolfe.exception.PropertiesFileNotFoundException;
import io.casswolfe.exception.SqlConnectionException;
import io.casswolfe.persistenceconfig.DatasourceLoader;
import io.casswolfe.struct.Customer;
import io.casswolfe.struct.Transaction;
import io.casswolfe.struct.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class DataTransformer {
    private final Logger log = LoggerFactory.getLogger(DataTransformer.class);
    private final int maxBatchSize;
    private final int generateCustomerAmount;

    public DataTransformer() {
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

        this.maxBatchSize = Integer.parseInt(prop.getProperty("app.max-batch-size"));
        this.generateCustomerAmount = Integer.parseInt(prop.getProperty("app.generate-customer-amount"));
    }

    public void fillData() {

        final Connection dbConnection = new DatasourceLoader().getConnection();
        try {
            List<String> customerIds = fillCustomerTable(dbConnection);
            fillVehicleAndTransactionTable(dbConnection, customerIds);
            dbConnection.commit();
        } catch (Exception e) {
            log.error("Error occurred. Rolling back transaction: {}", e.toString());
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                throw new SqlConnectionException("Failed to rollback transaction. Possibly transaction itself failed as well", ex);
            }
            throw new RuntimeException(e);
        }
    }

    private List<String> fillCustomerTable(Connection dbConnection) {
        List<String> customerIds = new ArrayList<>();
        try {
            List<String> fLines = Files.readAllLines(Path.of(ClassLoader.getSystemResource("firstnames.txt").toURI()));
            List<String> lLines = Files.readAllLines(Path.of(ClassLoader.getSystemResource("lastnames.txt").toURI()));
            int batchCount = 1;
            int i = 0;
            while (i < generateCustomerAmount) {

                String sqlStart = "insert into customer values (?,?,?,?,?);";
                try (PreparedStatement statement = dbConnection.prepareStatement(sqlStart)) {

                    while (batchCount <= maxBatchSize) {

                        int randomFist = new Random().nextInt(fLines.size());
                        int randomLast = new Random().nextInt(lLines.size());

                        String firstName = fLines.get(randomFist);
                        String lastName = lLines.get(randomLast);
                        Customer customer = new Customer(Long.toString(new Random().nextLong(1000000000L, 9999999999L)), firstName, lastName,
                                null, String.format("%s%s@mymail.com", subFour(firstName), subFour(lastName)));
                        statement.setString(1, customer.customerId());
                        statement.setString(2, customer.firstName());
                        statement.setString(3, customer.lastName());
                        statement.setString(4, customer.phoneNumber());
                        statement.setString(5, customer.emailAddress());
                        statement.addBatch();

                        customerIds.add(customer.customerId());
                        batchCount++;
                    }

                    statement.executeBatch();
                    log.info("Created {} customers", customerIds.size());
                    i += batchCount;
                    batchCount = 1;
                } catch (SQLException ex) {
                    log.error(ex.toString());
                    throw new RuntimeException(ex);
                }
                i++;
            }
        } catch (IOException | URISyntaxException ex) {
            throw new DataFileNotFoundException(ex);
        }
        return customerIds;
    }

    private void fillVehicleAndTransactionTable(Connection dbConnection, List<String> customerIds) {
        try {
            List<String> entries = Files.readAllLines(Path.of(ClassLoader.getSystemResource("carvana_car_sold-2022-08.csv").toURI()));
            Map<String, Set<String>> makesModels = new HashMap<>();
            Set<String> vehicleIds = new HashSet<>();

            int batchCount = 1;
            int i = 1 /*skip header row*/;
            while (i < entries.size()) {
                String vehicleInsert = "insert into vehicle values (?,?,?,?,?,?);";
                String transactionInsert = "insert into transaction values (?,?,?,?,?);";

                try (PreparedStatement transactionStatement = dbConnection.prepareStatement(transactionInsert);
                     PreparedStatement vehicleStatement = dbConnection.prepareStatement(vehicleInsert)) {

                    while (batchCount <= maxBatchSize) {
                        String[] entryValues = entries.get(i).replaceAll("\"", "").split(",");
                        Vehicle vehicle = new Vehicle(entryValues[0], Integer.parseInt(entryValues[2]), entryValues[3], entryValues[4], entryValues[6], Integer.parseInt(entryValues[5]));
                        if (!vehicleIds.contains(vehicle.vehicleId())) {
                            vehicleIds.add(vehicle.vehicleId());

                            vehicleStatement.setString(1, vehicle.vehicleId());
                            vehicleStatement.setInt(2, vehicle.year());
                            vehicleStatement.setString(3, vehicle.make());
                            vehicleStatement.setString(4, vehicle.model());
                            vehicleStatement.setInt(5, vehicle.miles());
                            vehicleStatement.setString(6, vehicle.trim());
                            vehicleStatement.addBatch();

                            Set<String> models = makesModels.get(vehicle.make());
                            if (models == null) {

                                String makeInsert = "insert into vehicle_make values (?);";

                                try (PreparedStatement makeStatement = dbConnection.prepareStatement(makeInsert)) {
                                    makeStatement.setString(1, vehicle.make());
                                    makeStatement.executeUpdate();

                                    makesModels.put(vehicle.make(), new HashSet<>());
                                    models = makesModels.get(vehicle.make());
                                }
                            }

                            if (!models.contains(vehicle.model())) {
                                String modelInsert = "insert into vehicle_model values (?,?);";

                                try (PreparedStatement modelStatement = dbConnection.prepareStatement(modelInsert)) {
                                    modelStatement.setString(1, vehicle.make());
                                    modelStatement.setString(2, vehicle.model());
                                    modelStatement.executeUpdate();

                                    models.add(vehicle.model());
                                }
                            }
                        }
                        Transaction transaction = new Transaction(UUID.randomUUID(), Integer.parseInt(entryValues[7]), LocalDate.parse(entryValues[12]),
                                entryValues[0], customerIds.get(new Random().nextInt(customerIds.size())));

                        transactionStatement.setObject(1, transaction.transactionId());
                        transactionStatement.setInt(2, transaction.soldPrice());
                        transactionStatement.setObject(3, transaction.soldDate());
                        transactionStatement.setString(4, transaction.vehicleId());
                        transactionStatement.setString(5, transaction.customerId());
                        transactionStatement.addBatch();

                        batchCount++;
                        i++;
                    }

                    vehicleStatement.executeBatch();
                    transactionStatement.executeBatch();
                    log.info("Uploaded {} sales transactions", i);
                    batchCount = 1;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new DataFileNotFoundException(e);
        }
    }

    private String subFour(String s) {
        if (s.length() > 3) {
            return s.substring(0, 4);
        }
        return s;
    }

}
