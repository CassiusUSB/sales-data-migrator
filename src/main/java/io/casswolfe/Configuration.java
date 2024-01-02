package io.casswolfe;

import io.casswolfe.exception.PropertiesFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    static final Properties prop = new Properties();

    public static void configure() {

        String profile = System.getenv("profile");
        LOG.info("Loading configuration profile {}", profile);
        try {
            if ("EC2".equals(profile)) {
                prop.load(ClassLoader.getSystemResourceAsStream("run-EC2.properties"));
            } else {
                prop.load(ClassLoader.getSystemResourceAsStream("run-local.properties"));
            }
        } catch (IOException e) {
            throw new PropertiesFileNotFoundException(e);
        }
        prop.putAll(System.getenv());
        prop.forEach((name, val) -> prop.put(sanitize((String) name), val));
    }

    private static String sanitize(String name) {
        return name.toLowerCase().replaceAll("_", ".");
    }

    public static String getenv(String name) {
        return prop.get(name).toString();
    }
}
