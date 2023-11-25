package io.casswolfe.struct;

import java.time.LocalDate;
import java.util.UUID;

public record Transaction(UUID transactionId, Integer soldPrice, LocalDate soldDate, String vehicleId, String customerId) {
}
