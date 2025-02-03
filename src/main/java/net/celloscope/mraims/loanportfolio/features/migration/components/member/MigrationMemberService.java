package net.celloscope.mraims.loanportfolio.features.migration.components.member;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationMemberService {

    private final MigrationMemberRepository migrationMemberRepository;
    private final ModelMapper modelMapper;

    public Mono<Member> update(Member member) {
            return migrationMemberRepository.findFirstByMemberIdOrderByMemberIdDesc(member.getMemberId())
                .flatMap(memberFromDB -> {
                    modelMapper.map(member, memberFromDB);
                    log.info("Member updated: {}", memberFromDB);
                    return migrationMemberRepository.save(memberFromDB);
                })
                .doOnError(throwable -> log.error("Error occurred while updating Member: {}", throwable.getMessage()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "Member not found")))
                .doOnError(throwable -> log.error("Error occurred while updating Member: {}", throwable.getMessage())
            );
    }

    public Mono<Member> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return migrationMemberRepository.findFirstByMemberIdOrderByMemberIdDesc(getMemberId(memberRequestDto))
                .switchIfEmpty(migrationMemberRepository.save(buildMember(getMemberId(memberRequestDto), memberRequestDto, requestDto.getMfiId(), requestDto.getOfficeId(), requestDto.getLoginId(), component.getPerson().getPersonId(), memberRequestDto.getRegisterBookSerialId())))
                .doOnNext(member -> log.info("Member with Member Id: {}", member.getMemberId()))
                .doOnSuccess(member -> log.info("Member saved: {}", member))
                .doOnError(throwable -> log.error("Error occurred while saving Member: {}", throwable.getMessage()));
    }

    private String getMemberId(MigrationMemberRequestDto memberRequestDto) {
        return memberRequestDto.getMemberId();
    }

    private Member buildMember(String id, MigrationMemberRequestDto memberRequestDto, String mfiId, String officeId, String loginId, String personId, String registrationBookSerialId) {
        return Member.builder()
//                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.MEMBER_SHORT_NAME.getValue() + "-" + UUID.randomUUID())   // This is the primary key
                .memberApplicationId(memberRequestDto.getSamityId() + "-" + memberRequestDto.getCompanyMemberId())  // *Required
                .memberId(id)  // *Required
                .companyMemberId(memberRequestDto.getCompanyMemberId())       // *input
                .registerBookSerialId(registrationBookSerialId)
                .memSmtOffPriMapId("1") //TODO: Need to update this from mem_smty_off_pri_map
                .personId(personId)
                .memberNameEn(memberRequestDto.getMemberName())   // *Required
                .memberNameBn(memberRequestDto.getMemberName())
                .dateOfBirth(memberRequestDto.getDateOfBirth())    // *Required
//                .ageOnAppDate("21")
                .gender("Male")    // *Required
                .religion("ISLAM")
                .nationality("BANGLADESHI") // *Required
                .bloodGroup("B+")
                .academicQualification("Graduate")
                .occupation("Business")
                .incomeSource("Business")
                .maritalStatus("Single")
//                .spouseNameEn("Spouse Doe")
//                .spouseNameBn("স্পাউস ডো")
//                .spouseContactNo("0123456789")
                .fatherNameEn("Father")
                .fatherNameBn("ফাদার")
                .motherNameEn("Mother")
                .motherNameBn("মাদার")
//                .remarks("Remarks")
                .noOfDependents(BigDecimal.ZERO)   // *Required for UI
                .mobile(memberRequestDto.getMobile())     // *Required
//                .email("johndoe@example.com")
//                .emergencyContactPerson("Emergency Contact")
//                .emergencyContactNumber("0123456789")
                .resDivisionId(memberRequestDto.getResDivisionId()) // *Required
                .resDistrictId(memberRequestDto.getResDistrictId())        // *Required
                .resUpazilaId(memberRequestDto.getResUpazilaId())     // *Required
                .resUnionId(memberRequestDto.getResUnionId())    // *Required
//                .resWardVillageStreet("Street 1")
//                .resPostOfficeId("01")
//                .resPostalCode("1000")
                .resAddressLine1(memberRequestDto.getResAddressLine1())  // *Required
//                .resAddressLine2("Address Line 2")
                .perDivisionId(memberRequestDto.getPerDivisionId()) // *Required
                .perDistrictId(memberRequestDto.getPerDistrictId())        // *Required
                .perUpazilaId(memberRequestDto.getPerUpazilaId())     // *Required
                .perUnionId(memberRequestDto.getPerUnionId())    // *Required
//                .perWardVillageStreet("Street 1")
//                .perPostOfficeId("01")
//                .perPostalCode("1000")
                .perAddressLine1(memberRequestDto.getPerAddressLine1())  // *Required
//                .perAddressLine2("Address Line 2")
//                .passbookNumber("123456")
                .identificationType("NID")  // *Required
                .nidNumber("1234567890123")
//                .smartCardIdNumber("1234567890")
//                .nidIssueDate(LocalDate.now())
//                .nidFrontDocId("01")
//                .nidBackDocId("01")
//                .birthRegNo("123456")
//                .birthIssueDate(LocalDate.now())
//                .birthRegDocId("01")
//                .passportNo("123456789")
//                .passportIssueDate(LocalDate.now())
//                .passportExpirationDate(LocalDate.now())
//                .passportDocId("01")
//                .drivingLicenseNo("123456789")
//                .drivingLicenseDocId("01")
//                .otherIdNo("123456789")
//                .otherDocName("Other Document")
//                .otherIdDocId("01")
//                .photoImageId("01")
//                .tinNo("123456789")
//                .tinDocId("01")
//                .loginId("johndoe")
//                .loginEnabledOn(LocalDateTime.now())
//                .memberPassword("password")
//                .microEnName("Micro Name")
//                .microLegalForm("Micro Legal Form")
                .gsInstallment(memberRequestDto.getGsInstallment())      // *Required
                .mfiId(mfiId)        // *Required
//                .submittedBy("User")
//                .submittedOn(LocalDateTime.now())
//                .currentVersion("1.0")
//                .isNewRecord("Yes")
//                .approvedBy("Approver")
//                .approvedOn(LocalDateTime.now())
//                .isApproverRemarks("Approver Remarks")
//                .approverRemarks("Approver Remarks")
//                .remarkedBy("User")
//                .remarkedOn(LocalDateTime.now())
                .status(MigrationEnums.STATUS_ACTIVE.getValue())       // *Required
                .migratedOn(LocalDateTime.now())
                .migratedBy(loginId)
                .createdBy(loginId)      // *Required
                .createdOn(LocalDateTime.now())        // *Required
//                .updatedBy()
//                .updatedOn(LocalDateTime.now())
                .build();
    }
}
