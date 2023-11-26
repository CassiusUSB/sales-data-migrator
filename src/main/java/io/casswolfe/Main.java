package io.casswolfe;

import io.casswolfe.service.DataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        new DataTransformer().fillData();
    }
}