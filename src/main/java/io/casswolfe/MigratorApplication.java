package io.casswolfe;

import io.casswolfe.service.DataTransformer;

public class MigratorApplication {

    public static void main(String[] args) {
       Configuration.configure();
        new DataTransformer().fillData();
    }
}