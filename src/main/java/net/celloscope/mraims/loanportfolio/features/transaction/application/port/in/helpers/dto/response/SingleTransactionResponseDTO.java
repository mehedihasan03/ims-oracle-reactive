package net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.redis.connection.ReactiveRedisConnection;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleTransactionResponseDTO {
    private String accountType;
    private String transactionId;
    private String processId;
    private String managementProcessId;
    private String memberId;
    private String mfiId;
    private String loginId;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String collectionType;
    private String withdrawType;
    private String transactionCode;
    private String paymentMode;
    private LocalDate transactionDate;
    private String transactedBy;
    private String status;
    private String officeId;
    private String samityId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
