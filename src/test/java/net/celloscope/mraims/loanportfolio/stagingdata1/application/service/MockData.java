/*
package net.celloscope.mraims.loanportfolio.stagingdata1.application.service;

import net.celloscope.mraims.loanportfolio.stagingdata1.domain.queries.LoanRepayScheduleDTO;
import net.celloscope.mraims.loanportfolio.stagingdata1.domain.queries.PassbookEntryDTOOld;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MockData {
	public static Mono<PassbookEntryDTOOld> getLastPassbookEntry(){
		return Mono.just(PassbookEntryDTOOld.builder()
				.installDate(LocalDate.parse("2023-02-18"))
				.installNo(3)
				.prinPaid(BigDecimal.valueOf(590.49))
				.serviceChargePaid(BigDecimal.valueOf(153.69))
				.prinRemainForThisInst(BigDecimal.valueOf(5.82))
				.scRemainForThisInst(BigDecimal.valueOf(0.00))
				.build());
	}
	
	public static Flux<LoanRepayScheduleDTO> getLoanRepaymentScheduleList(){
		return Flux.just(
				LoanRepayScheduleDTO.builder()
						.installDate(LocalDate.parse("2023-02-04"))
						.installNo(1)
						.principal(BigDecimal.valueOf(590.00))
						.serviceCharge(BigDecimal.valueOf(160.00))
						.status("paid")
						.build(),
				
				LoanRepayScheduleDTO.builder()
						.installDate(LocalDate.parse("2023-02-11"))
						.installNo(2)
						.principal(BigDecimal.valueOf(593.15))
						.serviceCharge(BigDecimal.valueOf(156.85))
						.status("paid")
						.build(),
				
				LoanRepayScheduleDTO.builder()
						.installDate(LocalDate.parse("2023-02-18"))
						.installNo(3)
						.principal(BigDecimal.valueOf(596.31))
						.serviceCharge(BigDecimal.valueOf(153.69))
						.status("pending")
						.build(),
				
				LoanRepayScheduleDTO.builder()
					.installDate(LocalDate.parse("2023-02-25"))
					.installNo(4)
					.principal(BigDecimal.valueOf(599.49))
					.serviceCharge(BigDecimal.valueOf(150.51))
					.status("pending")
					.build(),
			
			LoanRepayScheduleDTO.builder()
					.installDate(LocalDate.parse("2023-03-04"))
					.installNo(5)
					.principal(BigDecimal.valueOf(602.69))
					.serviceCharge(BigDecimal.valueOf(147.31))
					.status("pending")
					.build(),
			
			LoanRepayScheduleDTO.builder()
					.installDate(LocalDate.parse("2023-03-11"))
					.installNo(6)
					.principal(BigDecimal.valueOf(605.90))
					.serviceCharge(BigDecimal.valueOf(144.10))
					.status("pending")
					.build()
		);
	}
}
*/
