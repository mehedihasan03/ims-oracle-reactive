package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.in.hadler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.IAuthorizationUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoVoucherHandler {
    private final AutoVoucherUseCase useCase;

    public Mono<ServerResponse> createAndSaveAutoVoucherFromAISRequest(ServerRequest serverRequest) {
        return this.buildAutoVoucherRequestDTO()
                .flatMap(useCase::createAndSaveAutoVoucherFromAISRequest)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getAutoVoucherListByManagementProcessIdAndProcessId(ServerRequest serverRequest) {
        return useCase.getAutoVoucherListByManagementProcessIdAndProcessId(
                serverRequest.queryParam("managementProcessId").orElse(""),
                serverRequest.queryParam("processId").orElse(""))
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> deleteAutoVoucherListByManagementProcessIdAndProcessId(ServerRequest serverRequest) {
        return useCase.deleteAutoVoucherListByManagementProcessIdAndProcessId(
                        serverRequest.queryParam("managementProcessId").orElse(""),
                        serverRequest.queryParam("processId").orElse(""))
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getAutoVoucherDetailListByVoucherId(ServerRequest serverRequest) {
        return useCase.getAutoVoucherDetailListByVoucherId(
                        serverRequest.queryParam("voucherId").orElse(""))
                .collectList()
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<AutoVoucherRequestDTO> buildAutoVoucherRequestDTO() {
        return Mono.just(AutoVoucherRequestDTO
                .builder()
                .managementProcessId("managementProcessId")
                .processId("processId")
                .officeId("1035")
                .mfiId("M1001")
                .businessDate(LocalDate.now())
                .loginId("Emon")
                .aisRequestList(getJournalRequestDTOList())
                .build());
    }

    private List<JournalRequestDTO> getJournalRequestDTOList() {
        return List.of(
                JournalRequestDTO
                        .builder()
                        .journalType("LOAN_COLLECTION")
                        .description("LOAN_COLLECTION, Date: 2023-11-30")
                        .amount(BigDecimal.valueOf(4342.00))
                        .referenceNo("2023-11-30")
                        .journalProcess("Auto")
                        .officeId("1035")
                        .mfiId("M1001")
                        .createdBy("Emon")
                        .journalList(List.of(
                                Journal
                                    .builder()
                                        .description("LOAN_COLLECTION - CashOnHand")
                                        .debitedAmount(BigDecimal.valueOf(4342.00))
                                        .creditedAmount(BigDecimal.ZERO)
                                        .ledgerId("101001-1035")
                                    .build(),
                                Journal
                                        .builder()
                                        .description("LOAN_COLLECTION - ServiceChargeOutstanding")
                                        .debitedAmount(BigDecimal.valueOf(0))
                                        .creditedAmount(BigDecimal.valueOf(987.90))
                                        .ledgerId("104002-1035")
                                        .subledgerId("104002-1035-L0002")
                                        .build(),
                                Journal
                                        .builder()
                                        .description("LOAN_COLLECTION - PrincipalOutstanding")
                                        .debitedAmount(BigDecimal.valueOf(0))
                                        .creditedAmount(BigDecimal.valueOf(3354.10))
                                        .ledgerId("104003-1035")
                                        .subledgerId( "104003-1035-L0002")
                                        .build()

                        ))
                        .processId("13fb05a4-a108-42e2-9258-c805ac3329aa_LOAN_REPAY")
                        .build(),
                JournalRequestDTO
                        .builder()
                        .journalType("SC_PROVISION")
                        .description("SC_PROVISION, Date: 2023-11-30")
                        .amount(BigDecimal.valueOf(802.42))
                        .referenceNo("2023-11-30")
                        .journalProcess("Auto")
                        .officeId("1035")
                        .mfiId("M1001")
                        .createdBy("Emon")
                        .journalList(List.of(
                                Journal
                                        .builder()
                                        .description("SC_PROVISION - ServiceChargeOutstanding")
                                        .debitedAmount(BigDecimal.valueOf(802.42))
                                        .creditedAmount(BigDecimal.ZERO)
                                        .ledgerId("104002-1035")
                                        .subledgerId("104002-1035-L0002")
                                        .build(),
                                Journal
                                        .builder()
                                        .description("SC_PROVISION - ServiceChargeIncome")
                                        .debitedAmount(BigDecimal.ZERO)
                                        .creditedAmount(BigDecimal.valueOf(802.42))
                                        .ledgerId("401001-1035")
                                        .subledgerId("401001-1035-L0002")
                                        .build()

                        ))
                        .processId("13fb05a4-a108-42e2-9258-c805ac3329aa_SC_PROVISIONING")
                        .build()
        );
    }

}
