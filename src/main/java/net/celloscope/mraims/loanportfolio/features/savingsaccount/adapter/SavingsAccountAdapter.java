package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.entity.SavingsAccountEntity;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.repository.ISavingsAccountRepository;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountDto;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.out.ISavingsAccountPersistencePort;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_DPS;
import static net.celloscope.mraims.loanportfolio.core.util.enums.SavingsProductType.SAVINGS_TYPE_ID_FDR;

@Slf4j
@Component
@RequiredArgsConstructor
public class SavingsAccountAdapter implements ISavingsAccountPersistencePort {

    private final ISavingsAccountRepository repository;
    private final ModelMapper mapper;

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingsAccountFluxByMemberId(String memberId) {
        return repository.findAllByMemberId(memberId)
                .map(savingsAccount -> {
                    SavingsAccountResponseDTO dto = mapper.map(savingsAccount, SavingsAccountResponseDTO.class);
                    dto.setSavingsProductCode(savingsAccount.getSavingsProductId());
                    dto.setSavingsProductType(savingsAccount.getSavingsTypeId());
                    return dto;
                })
                .doOnNext(dto -> log.debug("Savings Account Response DTO: {}", dto));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> getSavingsAccountBySavingsAccountId(String savingsAccountId) {
        return repository
                .findBySavingsAccountId(savingsAccountId)
                .doOnRequest(value -> log.debug("Requesting to get savings account : {}", savingsAccountId))
                .doOnNext(savingsAccountResponseDTO -> log.debug("received savings account : {}", savingsAccountResponseDTO))
                .map(entity -> mapper.map(entity, SavingsAccountResponseDTO.class))
                .doOnNext(dto -> log.debug("SavingsAccountResponseDTO: {}", dto));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, LocalDate transactionDate, String loginId) {
        log.debug("status to be updated : {}", status);

        return repository
                .findBySavingsAccountId(savingsAccountId)
                .doOnRequest(value -> log.debug("Requesting to get savings account : {}, {}", savingsAccountId, status))
                .doOnNext(savingsAccountEntity -> log.debug("received savings account : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> {
                    LocalDate accountOpeningDate;

                    if (status.equalsIgnoreCase(Status.STATUS_ACTIVE.getValue())) {
                        accountOpeningDate = transactionDate;
                    }
                    else accountOpeningDate = savingsAccountEntity.getAcctStartDate();

                    savingsAccountEntity.setStatus(status);
                    savingsAccountEntity.setAcctStartDate(accountOpeningDate);
                    savingsAccountEntity.setUpdatedBy(loginId);
                    savingsAccountEntity.setUpdatedOn(LocalDateTime.now());
                    return savingsAccountEntity;
                })
//                .flatMap(this::updateSavingsAccountEntityMaturityAmount)
                .doOnNext(savingsAccountEntity -> log.debug("after setting savings account status : {}", savingsAccountEntity))
                .flatMap(repository::save)
                .doOnRequest(value -> log.debug("requesting to update savings account in db"))
                .doOnSuccess(savingsAccountEntity -> log.debug("successfully updated savings account status : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    private Mono<SavingsAccountEntity> updateSavingsAccountEntityMaturityAmount(SavingsAccountEntity savingsAccountEntity) {
        Mono<SavingsAccountEntity> savingsAccountEntityMono = Mono.just(savingsAccountEntity);

        if ((savingsAccountEntity.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue())
                || savingsAccountEntity.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue())) &&
                (savingsAccountEntity.getDepositTerm() == null || savingsAccountEntity.getDepositTermPeriod() == null))
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.EXPECTATION_FAILED, "Deposit Term & Deposit Term Period compulsory."));

        if (savingsAccountEntity.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_FDR.getValue())) {
            log.info("updating FDR maturity amount.");
            savingsAccountEntityMono = repository
                    .getFDRAccountDetailsBySavingsAccountId(savingsAccountEntity.getSavingsAccountId(), SAVINGS_TYPE_ID_FDR.getValue())
                    .map(fdrAccountDTO -> getFDRMaturityAmount(savingsAccountEntity, fdrAccountDTO.getInterestRate(), fdrAccountDTO.getInterestRateFrequency()))
                    .map(maturityAmount -> {
                        savingsAccountEntity.setMaturityAmount(maturityAmount);
                        return savingsAccountEntity;
                    });
        } else if (savingsAccountEntity.getSavingsTypeId().equalsIgnoreCase(SAVINGS_TYPE_ID_DPS.getValue())) {
            log.info("updating DPS maturity amount.");
            savingsAccountEntityMono = repository
                    .getDPSAccountDetailsBySavingsAccountId(savingsAccountEntity.getSavingsAccountId(), SAVINGS_TYPE_ID_DPS.getValue())
                    .map(dpsAccountDTO -> getDPSMaturityAmount(savingsAccountEntity, dpsAccountDTO.getInterestRate(), dpsAccountDTO.getInterestRateFrequency()))
                    .map(maturityAmount -> {
                        savingsAccountEntity.setMaturityAmount(maturityAmount);
                        return savingsAccountEntity;
                    });
        }
        return savingsAccountEntityMono;
    }

    private LocalDate getAcctEndDate(LocalDate acctStartDate, Integer depositTermInMonths) {
        return acctStartDate.plusMonths(depositTermInMonths);
    }

    private BigDecimal getFDRMaturityAmount(SavingsAccountEntity savingsAccountEntity, BigDecimal interestRate, String interestRateFrequency) {
        BigDecimal annualInterestRate = CommonFunctions
                .getAnnualInterestRate(interestRate, interestRateFrequency);
        log.info("annual interest rate : {}", annualInterestRate);
        Integer depositTermInMonths = getDepositTermInMonths(savingsAccountEntity.getDepositTerm(), savingsAccountEntity.getDepositTermPeriod());
        log.info("depositTermInMonths : {}", depositTermInMonths);
        LocalDate acctEndDate = getAcctEndDate(savingsAccountEntity.getAcctStartDate(),depositTermInMonths);
        log.info("acctEndDate : {}", acctEndDate);
        savingsAccountEntity.setAcctEndDate(acctEndDate);
        BigDecimal interestRatePerDay = annualInterestRate.divide(BigDecimal.valueOf(36500), 8, RoundingMode.HALF_UP);
        log.info("interestRatePerDay : {}", interestRatePerDay);
        long termLengthInDays = Math.abs(ChronoUnit.DAYS.between(savingsAccountEntity.getAcctStartDate(), acctEndDate));
        log.info("termLengthInDays : {}", termLengthInDays);
        BigDecimal totalInterest = CommonFunctions.round(savingsAccountEntity.getSavingsAmount().multiply(interestRatePerDay).multiply(BigDecimal.valueOf(termLengthInDays)), 0, RoundingMode.HALF_UP);
        log.info("totalInterest : {}", totalInterest);
        BigDecimal maturityAmount = savingsAccountEntity.getSavingsAmount().add(totalInterest);
        log.info("maturityAmount : {}", maturityAmount);

        return maturityAmount;
    }

    private Integer getDepositTermInMonths(Integer depositTerm, String depositTermPeriod) {
        int depositTermInMonths = 0;
        switch (depositTermPeriod.trim().toUpperCase()) {
            case "YEAR" -> depositTermInMonths = depositTerm * 12;
            case "MONTH" -> depositTermInMonths = depositTerm;
        }
        return depositTermInMonths;
    }

    public BigDecimal getDPSMaturityAmount(SavingsAccountEntity savingsAccountEntity, BigDecimal interestRate, String interestRateFrequency) {
        int dpsPeriod = getDPSPeriod(savingsAccountEntity.getDepositEvery(), savingsAccountEntity.getDepositTerm(), savingsAccountEntity.getDepositTermPeriod());
        BigDecimal annualInterestRate = CommonFunctions.getAnnualInterestRate(interestRate, interestRateFrequency);
        Integer depositTermInMonths = getDepositTermInMonths(savingsAccountEntity.getDepositTerm(), savingsAccountEntity.getDepositTermPeriod());
        log.info("depositTermInMonths : {}", depositTermInMonths);
        LocalDate acctEndDate = getAcctEndDate(savingsAccountEntity.getAcctStartDate(),depositTermInMonths);
        log.info("acctEndDate : {}", acctEndDate);
        savingsAccountEntity.setAcctEndDate(acctEndDate);

        return switch (savingsAccountEntity.getDepositEvery().trim().toUpperCase()) {
            case "MONTHLY" -> BigDecimal.valueOf(calculateMaturityAmountMonthlyDeposit(
                    savingsAccountEntity.getSavingsAmount().doubleValue(),
                    annualInterestRate.doubleValue(),
                    dpsPeriod));
            case "WEEKLY" -> BigDecimal.valueOf(calculateMaturityAmountWeeklyDeposit(
                    savingsAccountEntity.getSavingsAmount().doubleValue(),
                    annualInterestRate.doubleValue(),
                    dpsPeriod));
            default -> savingsAccountEntity.getSavingsAmount();
        };
    }

    private static double calculateMaturityAmountWeeklyDeposit(double weeklyDeposit, double annualInterestRate, int dpsPeriodWeeks) {
        double weeklyInterestRate = (annualInterestRate/100) / 52;
        double maturityAmount = weeklyDeposit * (((Math.pow(1 + weeklyInterestRate, dpsPeriodWeeks) - 1) / weeklyInterestRate) + Math.pow(1 + weeklyInterestRate, dpsPeriodWeeks));
        maturityAmount = maturityAmount - weeklyDeposit;

        maturityAmount = CommonFunctions.round(BigDecimal.valueOf(maturityAmount), 0, RoundingMode.HALF_UP).doubleValue();
        System.out.println("maturity amount weekly : " + maturityAmount);
        return maturityAmount;
    }

    private static double calculateMaturityAmountMonthlyDeposit(double monthlyDeposit, double annualInterestRate, int dpsPeriodMonths) {
        double monthlyInterestRate = (annualInterestRate/100) / 12;
        /*double monthlyInterestRateRounded = BigDecimal.valueOf(monthlyInterestRate).setScale(8, RoundingMode.HALF_UP).doubleValue();
        System.out.println("monthly interest rate rounded : " + monthlyInterestRateRounded);*/
        log.info("dpsPeriod : {}", dpsPeriodMonths);
        log.info("monthlyInterestRate : {}", monthlyInterestRate);

        double maturityAmount = monthlyDeposit * (((Math.pow(1 + monthlyInterestRate, dpsPeriodMonths) - 1) / monthlyInterestRate) + Math.pow(1 + monthlyInterestRate, dpsPeriodMonths));
        maturityAmount = maturityAmount - monthlyDeposit;
        log.info("maturityAmount unrounded : {}", maturityAmount);

        maturityAmount = CommonFunctions.round(BigDecimal.valueOf(maturityAmount), 0, RoundingMode.HALF_UP).doubleValue();
        System.out.println("maturity Amount Monthly : " + maturityAmount);
        return maturityAmount;
    }

    public Integer getDPSPeriod(String installmentFrequency, Integer depositTerm, String depositTermPeriod) {
        double dpsPeriod = 0;
        if (installmentFrequency.trim().equalsIgnoreCase("Monthly")) {
            switch (depositTermPeriod.trim().toUpperCase()) {
                case "YEAR" -> dpsPeriod = depositTerm * 12;
                case "MONTH" -> dpsPeriod = depositTerm;
            }
        } else if (installmentFrequency.trim().equalsIgnoreCase("Weekly")) {
            double depositTermInYear;
            switch (depositTermPeriod.trim().toUpperCase()) {
                case "YEAR" -> {
                    depositTermInYear = depositTerm;
                    dpsPeriod = depositTermInYear * 52;
                }
                case "MONTH" -> {
                    depositTermInYear = (double) depositTerm / 12;
                    dpsPeriod = depositTermInYear * 52;
                }
            }
        }

        Integer dpsPeriodInt = BigDecimal.valueOf(dpsPeriod).setScale(0, RoundingMode.HALF_UP).intValue();
        return dpsPeriodInt;
    }

    public Mono<String> getProductIdBySavingsAccountId(String savingsAccountId) {
        return repository.findBySavingsAccountId(savingsAccountId)
                .map(SavingsAccountEntity::getSavingsProductId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountStatus(String savingsAccountId, String status, LocalDate activationDate, LocalDate closingDate) {
        return repository
                .findBySavingsAccountId(savingsAccountId)
                .map(savingsAccountEntity -> {
                    savingsAccountEntity.setAcctStartDate(activationDate);
                    savingsAccountEntity.setAcctEndDate(closingDate);
                    savingsAccountEntity.setStatus(status);
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRSavingsAccountOnMaturity(String savingsAccountId) {
        return repository
                .findBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("matured account : {}", savingsAccountId))
                .map(savingsAccountEntity -> {
                    savingsAccountEntity.setMinBalance(BigDecimal.ZERO);
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getAllFDRAccountsEligibleForInterestPosting(LocalDate lastBusinessDate, LocalDate currentBusinessDate, String savingsTypeId) {
        return repository
                .findAllValidFDRAccountsInBetweenDaysOfMonths(lastBusinessDate, currentBusinessDate, savingsTypeId)
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingAccountByMemberIdList(List<String> memberIdList) {
        return repository.findAllByMemberIdList(memberIdList)
                .map(savingsAccount -> {
                    SavingsAccountResponseDTO dto = mapper.map(savingsAccount, SavingsAccountResponseDTO.class);
                    dto.setSavingsProductCode(savingsAccount.getSavingsProductId());
                    dto.setSavingsProductType(savingsAccount.getSavingsTypeId());
                    return dto;
                });
    }

    @Override
    public Flux<SavingsAccountResponseDTO> getSavingsAccountsByOfficeIdAndStatus(String officeId, String status) {
        return repository
                .getSavingsAccountByOfficeIdAndStatus(officeId, status)
                .map(savingsAccount -> mapper.map(savingsAccount, SavingsAccountResponseDTO.class));
    }

    @Override
    public Flux<FDRAccountDTO> getFDRSavingsAccountsByOfficeIdAndStatus(String officeId, List<String> statusList) {
        return repository
                .getFDRSavingsAccountsByOfficeIdAndStatus(officeId, statusList, SAVINGS_TYPE_ID_FDR.getValue());
    }

    @Override
    public Mono<FDRAccountDTO> getFDRAccountDetailsBySavingsAccountId(String savingsAccountId) {
        return repository
                .getFDRAccountDetailsBySavingsAccountId(savingsAccountId, SAVINGS_TYPE_ID_FDR.getValue());
    }


    @Override
    public Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeId(String officeId, List<String> statusList) {
        return repository
                .getDPSSavingsAccountsByOfficeIdAndStatus(officeId, statusList, SAVINGS_TYPE_ID_DPS.getValue());
    }

    @Override
    public Flux<DPSAccountDTO> getDPSSavingsAccountsByOfficeIdAndSearchText(String officeId, String searchText, List<String> statusList) {
        return repository
                .getDPSSavingsAccountsByOfficeIdAndStatusAndSearchText(officeId, searchText, statusList, SAVINGS_TYPE_ID_DPS.getValue());
    }

    @Override
    public Mono<DPSAccountDTO> getDPSAccountDetailsBySavingsAccountId(String savingsAccountId) {
        return repository
                .getDPSAccountDetailsBySavingsAccountId(savingsAccountId, SAVINGS_TYPE_ID_DPS.getValue());
    }

    @Override
    public Mono<SavingsAccountDto> getSavingsAccountInfoBySavingsAccountId(String savingsAccountId) {
        return repository
                .getSavingsAccountInfoBySavingsAccountId(savingsAccountId);
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatus(String savingsAccountId, String status, String loginId) {
        return repository
                .getSavingsAccountEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account found with Id : " + savingsAccountId)))
                .map(savingsAccountEntity -> {
                    savingsAccountEntity.setStatus(status);
                    savingsAccountEntity.setUpdatedOn(LocalDateTime.now());
                    savingsAccountEntity.setUpdatedBy(loginId);

                    if (status.equalsIgnoreCase(Status.STATUS_CLOSED.getValue())) {
                        savingsAccountEntity.setBalance(BigDecimal.ZERO);
                    }
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .doOnNext(savingsAccountEntity -> log.info("savings account entity after updating status : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountStatusForSavingsClosure(String savingsAccountId, LocalDate closeDate, String status, String loginId) {
        return repository
                .getSavingsAccountEntityBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No savings account found with Id : " + savingsAccountId)))
                .map(savingsAccountEntity -> {
                    savingsAccountEntity.setStatus(status);
                    savingsAccountEntity.setUpdatedOn(LocalDateTime.now());
                    savingsAccountEntity.setUpdatedBy(loginId);

                    if (status.equalsIgnoreCase(Status.STATUS_CLOSED.getValue())) {
                        savingsAccountEntity.setBalance(BigDecimal.ZERO);
                        savingsAccountEntity.setAcctEndDate(closeDate);
                    }
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .doOnNext(savingsAccountEntity -> log.info("savings account entity after update status : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountInterestPostingDates(List<LocalDate> interestPostingDates, String savingsAccountId, LocalDate acctStartDate, LocalDate acctEndDate, String loginId) {
        return repository
                .getSavingsAccountEntityBySavingsAccountId(savingsAccountId)
                .map(savingsAccountEntity -> {
                    savingsAccountEntity.setStatus(Status.STATUS_ACTIVE.getValue());
                    savingsAccountEntity.setInterestPostingDates(interestPostingDates.toString());
                    savingsAccountEntity.setAcctStartDate(acctStartDate);
                    savingsAccountEntity.setAcctEndDate(acctEndDate);
                    savingsAccountEntity.setUpdatedOn(LocalDateTime.now());
                    savingsAccountEntity.setUpdatedBy(loginId);
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateFDRDPSAccountMaturityAmount(String savingsAccountId, BigDecimal maturityAmount) {
        return repository
                .getSavingsAccountEntityBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("requesting to get savings account entity by id : {}", savingsAccountId))
                .map(savingsAccountEntity -> {
                    log.info("setting maturity amount : {}", maturityAmount);
                    savingsAccountEntity.setMaturityAmount(maturityAmount);
                    savingsAccountEntity.setStatus(Status.STATUS_ACTIVE.getValue());
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .doOnNext(savingsAccountEntity -> log.info("savings account entity after updating maturity amount : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

    @Override
    public Mono<SavingsAccountResponseDTO> updateSavingsAccountBalance(String savingsAccountId, BigDecimal balance, String status) {
        return repository
                .getSavingsAccountEntityBySavingsAccountId(savingsAccountId)
                .doOnRequest(l -> log.info("requesting to get savings account entity by id : {}", savingsAccountId))
                .doOnNext(savingsAccountEntity -> log.info("savings account entity : {}", savingsAccountEntity))
                .map(savingsAccountEntity -> {
                    log.info("setting balance : {}", balance);
                    savingsAccountEntity.setBalance(balance);
                    savingsAccountEntity.setStatus(status);
                    return savingsAccountEntity;
                })
                .flatMap(repository::save)
                .map(savingsAccountEntity -> mapper.map(savingsAccountEntity, SavingsAccountResponseDTO.class));
    }

}
