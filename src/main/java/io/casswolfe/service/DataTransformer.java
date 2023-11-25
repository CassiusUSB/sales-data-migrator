package io.casswolfe.service;

import io.casswolfe.persistenceconfig.DatasourceLoader;
import io.casswolfe.struct.Customer;
import io.casswolfe.struct.Transaction;
import io.casswolfe.struct.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class DataTransformer {
    private static final Logger LOG = LoggerFactory.getLogger("DataTransformer.class");

    public void fillData() {
        String profile = System.getenv("profile");
        Properties prop = new Properties();

        try {
            if ("EC2".equals(profile)) {
                prop.load(ClassLoader.getSystemResourceAsStream("EC2.properties"));
            } else {
                prop.load(ClassLoader.getSystemResourceAsStream("local.properties"));


            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Connection dbConnection = new DatasourceLoader().getS(prop);

        try {
            if ("EC2".equals(profile)) {
                prop.load(ClassLoader.getSystemResourceAsStream("EC2.properties"));
            } else {
                prop.load(ClassLoader.getSystemResourceAsStream("local.properties"));

                List<String> customerIds = fillCustomers(dbConnection);


                List<String> line = Files.readAllLines(Path.of(ClassLoader.getSystemResource("carvana_car_sold-2022-08.csv").getPath()));
                Map<String, Set<String>> makesModels = new HashMap<>();

                for (int i = 1 /*skip header row*/; i < line.size(); i++) {
                    String[] tokens = line.get(i).replaceAll("\"", "").split(",");
                    Vehicle vehicle = new Vehicle(tokens[0], Integer.parseInt(tokens[2]), tokens[3], tokens[4], tokens[6], Integer.parseInt(tokens[5]));

                    makesModels.putIfAbsent(vehicle.make(), new HashSet<>());
                    makesModels.get(vehicle.make()).add(vehicle.model());

                    Transaction transaction = new Transaction(UUID.randomUUID(), Integer.parseInt(tokens[7]), LocalDate.parse(tokens[12]),
                            tokens[0], customerIds.get(new Random().nextInt(customerIds.size())));


                }
            }
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    private List<String> fillCustomers(Connection dbConnection) {
        List<String> customerIds = new ArrayList<>();
        try {
            List<String> fLines = Files.readAllLines(Path.of(ClassLoader.getSystemResource("firstnames.txt").getPath()));
            List<String> lLines = Files.readAllLines(Path.of(ClassLoader.getSystemResource("lastnames.txt").getPath()));

            for (int i = 0; i < 30000; i++) {
                int randomFist = new Random().nextInt(fLines.size());
                int randomLast = new Random().nextInt(lLines.size());

                String firstName = fLines.get(randomFist);
                String lastName = lLines.get(randomLast);
                Customer customer = new Customer(Integer.toString(new Random().nextInt(100000000, 999999999)), firstName, lastName,
                        null, String.format("%s%s@mymail.com", subFour(firstName), subFour(lastName)));


                String sql = "insert into customer values (?,?,?,?,?)";

                try (PreparedStatement statement = dbConnection.prepareStatement(sql)) {

                    statement.setString(1, customer.customerId());
                    statement.setString(2, customer.firstName());
                    statement.setString(3, customer.lastName());
                    statement.setString(4, customer.phoneNumber());
                    statement.setString(5, customer.emailAddress());
                    statement.executeUpdate();

                    customerIds.add(customer.customerId());
                    if (i % 1000 == 0) {
                        LOG.info("Uploaded {} rows", i);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return customerIds;
    }

    private String subFour(String s) {
        if (s.length() > 3) {
            return s.substring(0, 4);
        }
        return s;
    }
}
