/*
package net.celloscope.mraims.loanportfolio.stagingdata1.application.service;

import com.google.gson.Gson;
import net.celloscope.mraims.loanportfolio.stagingdata1.domain.LoanAccountInfo;
import net.celloscope.mraims.loanportfolio.stagingdata1.domain.queries.LoanRepayScheduleDTO;
import net.celloscope.mraims.loanportfolio.stagingdata1.domain.queries.PassbookEntryDTOOld;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileReader;
import java.io.Reader;

public class StagingDataServiceTests {
	
	@Test
	public void PopulateStagingDataWithDueTest(){
		Mono<PassbookEntryDTOOld> passbook = MockData.getLastPassbookEntry();
		
		Flux<LoanRepayScheduleDTO> lrs = MockData.getLoanRepaymentScheduleList();
		
		LoanAccountInfoServiceOld service = new LoanAccountInfoServiceOld();
		service.populateStagingDataWithDue(passbook, lrs).subscribe();
		
	}
	
	@Test
	public void PopulateStagingDataWithAdvanceTest(){
		Mono<PassbookEntryDTOOld> passbook = MockData.getLastPassbookEntry();
		
		Flux<LoanRepayScheduleDTO> lrs = MockData.getLoanRepaymentScheduleList();
		
		LoanAccountInfoServiceOld service = new LoanAccountInfoServiceOld();
		service.populateStagingDataWithAdvance(passbook, lrs).subscribe();
	}
	
	@Test
	public void GetStagingDataForOneActiveLoanIDTest(){
		
		Mono<PassbookEntryDTOOld> passbook = MockData.getLastPassbookEntry();
		
		Flux<LoanRepayScheduleDTO> lrs = MockData.getLoanRepaymentScheduleList();
		
		LoanAccountInfoServiceOld service = new LoanAccountInfoServiceOld();
		service.getInstallmentListForOneActiveLoanAccount(passbook, lrs).subscribe();
	}
	
	@Test
	public void sudoTest(){
		Gson gson = new Gson();
		try(Reader reader = new FileReader("./src/test/resources/PassbookEntryData.json")){
			PassbookEntryDTOOld[] passbookEntryDTOArray = gson.fromJson(reader, PassbookEntryDTOOld[].class);
			for (PassbookEntryDTOOld p: passbookEntryDTOArray) {
				System.out.println(p);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void DTOTest(){
		LoanAccountInfo dto = LoanAccountInfo.builder()
				.loanAccountId("12345")
				.build();
		System.out.println("DTO: " + dto);
	}
}

*/
