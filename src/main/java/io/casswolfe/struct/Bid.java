package io.casswolfe.struct;

import java.time.LocalDateTime;
import java.util.UUID;

public record Bid(UUID bid_id, LocalDateTime bidTimestamp, String dollar_amount, String customer_id, String emailAddress) {
}
