package net.celloscope.mraims.loanportfolio.features.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class  MigrationUtils {
    public  static <T> Mono<String> generateId(Mono<T> findFirstByOrderByIdDesc, Function<T, String> getId, String splitPattern, String formatPattern) {
        return findFirstByOrderByIdDesc
                .map(getId)
                .doOnNext(s -> log.info("Last id: {}", s))
                .map(id -> {
                    String[] split = id.split(splitPattern);
                    log.info("Split: {}", split[0]);
                    return split[split.length - 1];
                })
                .defaultIfEmpty("0")
                .map(s -> {
                    int number = Integer.parseInt(s) + 1;
                    return String.format(formatPattern, number);
                }).doOnNext(s -> log.info("Generated id: {}", s));
    }

    public static BigDecimal getLoanTermInYears(Integer loanTermInMonth) {
        if (loanTermInMonth == null)
            return BigDecimal.ONE;
        return BigDecimal.valueOf(loanTermInMonth).divide(BigDecimal.valueOf(12));
    }

    public static BigDecimal calculatePrincipal(String memberId, BigDecimal disbursedLoanAmount, Integer principalAmountPrecision, BigDecimal scRate, Integer loanTermInMonth, Integer serviceChargeRatePrecision, String roundingLogic) {
        log.info("log for {} | Calculating principal amount for disbursed loan amount: {}, service charge rate: {}, rounding logic: {}", memberId, disbursedLoanAmount, scRate, roundingLogic);
        BigDecimal principalAmount =  disbursedLoanAmount.divide(
            (scRate.multiply(getLoanTermInYears(loanTermInMonth))).divide(
                        BigDecimal.valueOf(100), serviceChargeRatePrecision, RoundingLogic.fromString(roundingLogic)).add(BigDecimal.ONE),
                principalAmountPrecision, RoundingLogic.fromString(roundingLogic));
        log.info("log for {} | Principal amount calculated: {}", memberId, principalAmount);
        return principalAmount;
    }

    public static BigDecimal calculateServiceCharge(String memberId, BigDecimal disbursedLoanAmount, BigDecimal principal, Integer serviceChargeRatePrecision, String roundingLogic) {
//        log.info("log for {} | Calculating service charge for disbursed loan amount: {}, principal: {}, rounding logic: {}", memberId, disbursedLoanAmount, principal, roundingLogic);
        BigDecimal serviceCharge = disbursedLoanAmount.subtract(principal).setScale(serviceChargeRatePrecision, RoundingLogic.fromString(roundingLogic));
        log.info("log for {} | Service charge calculated: {}", memberId, serviceCharge);
        return serviceCharge;
    }

    public static BigDecimal calculateInstallmentAmount(String memberId, BigDecimal disbursedLoanAmount, Integer noInstallment, Integer installmentAmountPrecision, String roundingLogic) {
//        log.info("log for {} | Calculating installment amount for disbursed loan amount: {}, no of installments: {}, rounding logic: {}", memberId, disbursedLoanAmount, noInstallment, roundingLogic);
        BigDecimal installmentAmount = disbursedLoanAmount.divide(BigDecimal.valueOf(noInstallment), installmentAmountPrecision, RoundingLogic.fromString(roundingLogic));
        log.info("log for {} | Installment amount calculated: {}", memberId, installmentAmount);
        return installmentAmount;
    }

    public static Integer calculateNoOfPastInstallments(String memberId, BigDecimal disbursedLoanAmount, BigDecimal loanOutstanding, BigDecimal overDue, Integer noInstallment, Integer installmentAmountPrecision, String roundingLogic) {
//        log.info("log for {} | Calculating no of past installments for disbursed loan amount: {}, loan outstanding: {}, overdue: {}", memberId, disbursedLoanAmount, loanOutstanding, overDue);
        BigDecimal totalInstallmentPaid = disbursedLoanAmount.subtract(loanOutstanding).add(overDue);
//        BigDecimal noOfPastInstallments = totalInstallmentPaid.divideToIntegralValue(calculateInstallmentAmount(memberId, disbursedLoanAmount, noInstallment, installmentAmountPrecision, roundingLogic));
        BigDecimal noOfPastInstallments = totalInstallmentPaid.divideToIntegralValue(calculateInstallmentAmount(memberId, disbursedLoanAmount, noInstallment, installmentAmountPrecision, roundingLogic));
        if (noOfPastInstallments.doubleValue() % 1 != 0) {
            log.warn("log for {} | No of past installments is fraction number", memberId);
            noOfPastInstallments = overDue.compareTo(BigDecimal.ZERO) == 0 ? noOfPastInstallments.setScale(0, RoundingMode.DOWN) :
                noOfPastInstallments;
        }
        if (noOfPastInstallments.doubleValue() % 1 != 0) {
            log.error("log for {} | No of past installments is fraction number (while overdue amount is non zero)", memberId);
            throw new RuntimeException("No of past installments is fraction number");
        }
        log.info("log for {} | No of past installments calculated: {}", memberId, noOfPastInstallments);
        return noOfPastInstallments.toBigInteger().intValue();
    }

    public static Integer calculateNoOfPaidDpsInstallment(String memberId, BigDecimal depositAmount, BigDecimal installmentAmount) {
//        log.info("log for {} | Calculating no of paid dps installments for deposit amount: {}, installment amount: {}", memberId, depositAmount, installmentAmount);
        Integer noOfPaidDpsInstallments = depositAmount.divideToIntegralValue(installmentAmount).intValue();
        log.info("log for {} | No of paid dps installments calculated: {}", memberId, noOfPaidDpsInstallments);
        return noOfPaidDpsInstallments;
    }

    public static String getWeekday(LocalDate date) {
        String dayOfWeekStr =  date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return dayOfWeekStr.substring(0, 1).toUpperCase() + dayOfWeekStr.substring(1);
    }

    public static Tuple2<Boolean, String> validateRequestedMemberDataBeforeMigration(MigrationMemberRequestDto memberRequestDto) {
        if (memberRequestDto.getLoanInformation() != null) {

            if (memberRequestDto.getLoanInformation().getInstallmentAmount() != null
                    && memberRequestDto.getLoanInformation().getInstallmentAmount().compareTo(BigDecimal.ZERO) > 0) {
                log.warn("log for {} | Installment amount is provided : {}", memberRequestDto.getMemberId(), memberRequestDto.getLoanInformation().getInstallmentAmount());
                return Tuples.of(true, "Installment amount is provided");
            }
            double disbursedLoanAmount = memberRequestDto.getLoanInformation().getDisbursedLoanAmount().doubleValue();
            Integer noInstallment = memberRequestDto.getLoanInformation().getNoInstallment();
            double installmentAmount = disbursedLoanAmount / noInstallment;
            log.info("log for {} | Disbursed Loan Amount: {}, No Of Installment: {}, Installment Amount: {}", memberRequestDto.getMemberId(), disbursedLoanAmount, noInstallment, installmentAmount);
            if (installmentAmount % 1 != 0) {
                log.error("log for {} | Installment amount is fraction number", memberRequestDto.getMemberId());
                return Tuples.of(false, "Installment amount is fraction number");
            }
            double loanOutstanding = memberRequestDto.getLoanInformation().getLoanOutstanding().doubleValue();
            double paidAmount = disbursedLoanAmount - loanOutstanding;
            log.info("log for {} | Paid Amount: {}", memberRequestDto.getMemberId(), paidAmount);
            log.info("log for {} | Paid Amount: {}, Installment Amount: {}, Paid Installment No: {}", memberRequestDto.getMemberId(), paidAmount, installmentAmount, paidAmount / installmentAmount);
            if (paidAmount % installmentAmount != 0) {
                log.warn("log for {} | No of Paid Installment is fraction number", memberRequestDto.getMemberId());
//            return Tuples.of(false, "No of Paid Installment is fraction number");
            }
            log.info("log for {} | Overdue Amount: {}, Installment Amount: {}, Overdue Installment No: {}",
                    memberRequestDto.getMemberId(), memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue(), installmentAmount, memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue() / installmentAmount);
            if (memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue() % installmentAmount != 0) {
                log.warn("log for {} | No of Overdue Installment is fraction number", memberRequestDto.getMemberId());
//            return Tuples.of(false, "No of Paid Overdue is fraction number");
            }
            log.info("log for {} | Paid Amount {} Plus Overdue Installment {} = {} divided by installment amount {}  is no of Past Installment {}", memberRequestDto.getMemberId(),
                paidAmount, memberRequestDto.getLoanInformation().getOverDueAmount(), memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue() + paidAmount,
                installmentAmount, (paidAmount + memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue()) / installmentAmount);
            if ((memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue() + paidAmount) % installmentAmount != 0) {
                if (memberRequestDto.getLoanInformation().getOverDueAmount().compareTo(BigDecimal.ZERO) != 0) {
                    log.error("log for {} | Paid Amount {} Plus Overdue Installment {} = {} divided by installment amount {}  is fraction number {}", memberRequestDto.getMemberId(),
                        paidAmount, memberRequestDto.getLoanInformation().getOverDueAmount(), memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue() + paidAmount,
                        installmentAmount, (paidAmount + memberRequestDto.getLoanInformation().getOverDueAmount().doubleValue()) / installmentAmount);
                    return Tuples.of(false, "No of Paid Amount Plus Overdue Installment divided by installment amount is fraction number");
                } else {
                    log.warn("log for {} | Assuming Partial Advance Payment", memberRequestDto.getMemberId());
                }
            }
            return Tuples.of(true, "Data is valid");
        }
        return Tuples.of(true, "Data is valid and no Loan Information Found");
    }
}
