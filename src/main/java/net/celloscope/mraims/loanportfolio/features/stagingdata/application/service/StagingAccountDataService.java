package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.Installment;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.IStagingAccountDataCommands;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.LoanRepayScheduleDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands.PassbookEntryDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

import static net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil.amountIfNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class StagingAccountDataService implements IStagingAccountDataCommands {
	private static Flux<Installment> getInstallmentListForOneActiveLoanAccount(Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {

		return lastPassbookEntry
				.flatMapMany(p -> {
					if (p == null || p.getInstallDate() == null || p.getInstallDate().isBefore(businessDate)) {
						return populateStagingDataWithDue(Mono.just(p), getLoanRepayScheduleListWithDue(loanRepayScheduleList, businessDate), businessDate);
					} else {
						return populateStagingDataWithAdvance(Mono.just(p), getLoanRepayScheduleListWithAdvance(lastPassbookEntry, loanRepayScheduleList, businessDate), businessDate);
					}
				});
	}
	
	//	Populate Staging Data when Last_Passbook_Entry < Current_Date with Due Payment
	private static Flux<Installment> populateStagingDataWithDue(Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {
		
		return loanRepayScheduleList
				.flatMap(lrs -> Mono.just(lrs).zipWith(lastPassbookEntry))
				.map(t -> {
					Installment installment = getInstallmentInfo(t);
					if (Objects.equals(t.getT1().getInstallNo(), t.getT2().getInstallNo())) {
//                        installment.setIsCurrent("Yes");
						installment.setDue(t.getT2().getPrinRemainForThisInst().add(t.getT2().getScRemainForThisInst()));
					} else {
						installment.setDue(t.getT1().getPrincipal().add(t.getT1().getServiceCharge()));
					}
					if (t.getT1().getInstallDate().isEqual(businessDate)) {
						installment.setIsCurrent("Yes");
					}
					return installment;
				})
				.doOnNext(i -> log.debug("Installment: {}", i));
	}
	
	//	Populate Staging data when Last_Passbook_Entry >= Current_Date with Advance Payment
	private static Flux<Installment> populateStagingDataWithAdvance(Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {
		return loanRepayScheduleList
				.flatMap(lrs -> Mono.just(lrs).zipWith(lastPassbookEntry))
				.doOnNext(t -> log.debug("LRS Installment Date: {}, Passbook Installment Date: {}", t.getT1().getInstallDate(), t.getT2().getInstallDate()))
				.map(t -> {
					Installment installment = getInstallmentInfo(t);
					
					if (t.getT2().getInstallDate().isEqual(t.getT1().getInstallDate())) {
//                        installment.setDue(t.getT2().getPrinRemainForThisInst().add(t.getT2().getScRemainForThisInst()));
						installment.setAdvance(t.getT2().getPrinPaid().add(t.getT2().getServiceChargePaid()));
					} else {
						installment.setAdvance(t.getT1().getPrincipal().add(t.getT1().getServiceCharge()));
					}
					if (t.getT1().getInstallDate().isEqual(businessDate)) {
						installment.setIsCurrent("Yes");
					}
					return installment;
				})
				.doOnNext(i -> log.debug("Installment: {}", i));
	}
	
	private static Flux<LoanRepayScheduleDTO> getLoanRepayScheduleListWithDue(Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {
		return loanRepayScheduleList
				.filter(lrs -> (lrs.getInstallDate().isEqual(businessDate) || lrs.getInstallDate().isBefore(businessDate)) && lrs.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
				.doOnNext(lrs -> log.debug("Loan Repay tSchedule List: {}", lrs));
	}
	
	private static Flux<LoanRepayScheduleDTO> getLoanRepayScheduleListWithAdvance(Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {
		return loanRepayScheduleList
				.flatMap(lrs -> Mono.just(lrs).zipWith(lastPassbookEntry))
				.filter(t -> !t.getT1().getInstallDate().isBefore(businessDate) && t.getT1().getInstallNo() <= t.getT2().getInstallNo())
				.map(Tuple2::getT1)
				.doOnNext(lrs -> log.debug("Loan Repay Schedule List: {}", lrs));
	}
	
	private static Installment getInstallmentInfo(Tuple2<LoanRepayScheduleDTO, PassbookEntryDTO> t) {
		return Installment.builder()
				.loanRepayScheduleId(t.getT1().getLoanRepayScheduleId())
				.installmentNo(t.getT1().getInstallNo())
				.installmentDate(t.getT1().getInstallDate())
				.installmentAmount(t.getT1().getTotalPayment())
				.due(BigDecimal.valueOf(0.00))
				.advance(BigDecimal.valueOf(0.00))
				.penalty(amountIfNull(t.getT1().getPenalty()))
				.fees(amountIfNull(t.getT1().getFees()))
				.insurance(amountIfNull(t.getT1().getInsurance()))
				.isCurrent("No")
				.build();
	}
	
	@Override
	public Mono<StagingAccountData> generateStagingAccountDataForOneActiveLoanAccount(Mono<StagingAccountData> stagingAccountData, Mono<PassbookEntryDTO> lastPassbookEntry, Flux<LoanRepayScheduleDTO> loanRepayScheduleList, LocalDate businessDate) {
		Flux<Installment> installmentList = getInstallmentListForOneActiveLoanAccount(lastPassbookEntry, loanRepayScheduleList, businessDate);
		
		Mono<BigDecimal> totalDue = installmentList
				.map(Installment::getDue)
				.reduce(BigDecimal.valueOf(0.0), BigDecimal::add);
		Mono<BigDecimal> totalAdvance = installmentList
				.map(Installment::getAdvance)
				.reduce(BigDecimal.valueOf(0.0), BigDecimal::add);
		Mono<BigDecimal> totalPrincipalPaid = lastPassbookEntry
				.map(p -> amountIfNull(p.getPrinPaidTillDate()));
		Mono<BigDecimal> totalServiceChargePaid = lastPassbookEntry
				.map(p -> amountIfNull(p.getScPaidTillDate()));
		
		return Mono.zip(stagingAccountData, totalDue, totalAdvance, totalPrincipalPaid, totalServiceChargePaid, installmentList.collectList())
				.map(t -> {
					t.getT1().setTotalDue(t.getT2());
					t.getT1().setTotalAdvance(t.getT3());
					t.getT1().setTotalPrincipalPaid(t.getT4());
					t.getT1().setTotalServiceChargePaid(t.getT5());
					t.getT1().setTotalPrincipalRemaining(t.getT1().getLoanAmount().subtract(t.getT4()));
					t.getT1().setTotalServiceChargeRemaining(t.getT1().getServiceCharge().subtract(t.getT5()));
					t.getT1().setInstallments(t.getT6());
					return t.getT1();
				})
				.flatMap(stagingAccountData1 -> loanRepayScheduleList
						.filter(lrs -> lrs.getStatus().equalsIgnoreCase(Status.STATUS_PENDING.getValue()))
						.sort(Comparator.comparing(LoanRepayScheduleDTO::getInstallNo))
						.next()
						.map(loanRepayScheduleDTO -> {
							stagingAccountData1.setScheduledInstallmentAmount(loanRepayScheduleDTO.getTotalPayment());
							return stagingAccountData1;

						}))
				.doOnNext(stagingAccountData1 -> log.debug("Staging Account Data Generated With InstallmentList: {}", stagingAccountData1));
	}
	
}
