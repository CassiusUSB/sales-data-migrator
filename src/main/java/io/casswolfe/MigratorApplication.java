package io.casswolfe;

import io.casswolfe.service.DataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigratorApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MigratorApplication.class);

    public static void main(String[] args) {
        new DataTransformer().fillData();
    }
}