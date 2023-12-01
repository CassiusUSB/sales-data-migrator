package io.casswolfe;

import io.casswolfe.exception.PropertiesFileNotFoundException;

import java.io.IOException;
import java.util.Properties;

public class Configuration {

    static final Properties prop = new Properties();

    public static void configure() {

        String profile = System.getenv("profile");
        try {
            if ("EC2".equals(profile)) {
                prop.load(ClassLoader.getSystemResourceAsStream("EC2.properties"));
            } else {
                prop.load(ClassLoader.getSystemResourceAsStream("local.properties"));
            }
        } catch (IOException e) {
            throw new PropertiesFileNotFoundException(e);
        }
        prop.putAll(System.getenv());
    }

    public static String getenv(String name) {
       return prop.get(name).toString();
    }
}
