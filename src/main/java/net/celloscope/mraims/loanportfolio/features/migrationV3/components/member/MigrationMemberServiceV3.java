package net.celloscope.mraims.loanportfolio.features.migrationV3.components.member;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationMemberServiceV3 {

    private final MigrationMemberRepositoryV3 migrationMemberRepository;
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
                .switchIfEmpty(migrationMemberRepository.save(buildMember(getMemberId(memberRequestDto), memberRequestDto, requestDto.getMfiId(), requestDto.getOfficeId(), requestDto.getLoginId(), component.getPerson().getPersonId(), memberRequestDto.getRegisterBookSerialId(), requestDto)))
                .doOnNext(member -> log.info("Member with Member Id: {}", member.getMemberId()))
                .doOnSuccess(member -> log.info("Member saved: {}", member))
                .doOnError(throwable -> log.error("Error occurred while saving Member: {}", throwable.getMessage()));
    }

    private String getMemberId(MigrationMemberRequestDto memberRequestDto) {
        return memberRequestDto.getMemberId();
    }

    private Member buildMember(String id, MigrationMemberRequestDto memberRequestDto, String mfiId, String officeId, String loginId, String personId, String registrationBookSerialId, MigrationRequestDto requestDto) {
        return Member.builder()
//                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.MEMBER_SHORT_NAME.getValue() + "-" + UUID.randomUUID())   // This is the primary key
                .memberApplicationId(memberRequestDto.getSamityId() + "-" + memberRequestDto.getCompanyMemberId())  // *Required
                .memberId(id)  // *Required
                .companyMemberId(memberRequestDto.getCompanyMemberId())       // *input
                .registerBookSerialId(registrationBookSerialId)
                .memSmtOffPriMapId("1") //TODO: Need to update this from mem_smty_off_pri_map
                .personId(personId)
                .memberNameEn(memberRequestDto.getMemberNameEn())   // *Required
                .memberNameBn(memberRequestDto.getMemberNameBn())
                .dateOfBirth(memberRequestDto.getDateOfBirth())    // *Required
//                .ageOnAppDate("21")
                .gender(memberRequestDto.getGender()!=null ? memberRequestDto.getGender():"Male")    // *Required
                .religion(memberRequestDto.getReligion()!=null?memberRequestDto.getReligion():"Islam")
                .nationality(memberRequestDto.getNationality()!=null ? memberRequestDto.getNationality():"Bangladeshi") // *Required
                .bloodGroup(memberRequestDto.getBloodGroup()!=null ? memberRequestDto.getBloodGroup():"A+")
                .academicQualification(memberRequestDto.getAcademicQualification()!=null ? memberRequestDto.getAcademicQualification():"SSC")
                .occupation(memberRequestDto.getOccupation() != null ? memberRequestDto.getOccupation():"Business")
                .incomeSource("Business")
                .maritalStatus(memberRequestDto.getMaritalStatus() !=null ? memberRequestDto.getMaritalStatus(): "Single")
                .spouseNameEn(memberRequestDto.getSpouseName() !=null ? memberRequestDto.getSpouseName(): "Spouse Doe")
//                .spouseNameBn("স্পাউস ডো")
//                .spouseContactNo("0123456789")
                .fatherNameEn(memberRequestDto.getFatherNameEn()!=null ? memberRequestDto.getFatherNameEn():"Father")
                .fatherNameBn(memberRequestDto.getFatherNameBn()!=null ? memberRequestDto.getFatherNameBn():"ফাদার")
                .motherNameEn(memberRequestDto.getMotherNameEn()!=null ? memberRequestDto.getMotherNameEn():"Mother")
                .motherNameBn(memberRequestDto.getMotherNameBn() !=null ? memberRequestDto.getMotherNameBn(): "মাদার")
                .remarks(memberRequestDto.getRemarks()!= null ? memberRequestDto.getRemarks():"Remarks")
                .noOfDependents(memberRequestDto.getNoOfDependent()!=null ? memberRequestDto.getNoOfDependent():BigDecimal.ZERO)   // *Required for UI
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
                .nidNumber(memberRequestDto.getNidNumber()!= null ? memberRequestDto.getNidNumber():" ")  // *Required
                .smartCardIdNumber(memberRequestDto.getSmartCardIdNo())
//                .nidIssueDate(LocalDate.now())
//                .nidFrontDocId("01")
//                .nidBackDocId("01")
                .birthRegNo(memberRequestDto.getBirthRegNo())
//                .birthIssueDate(LocalDate.now())
//                .birthRegDocId("01")
                .passportNo(memberRequestDto.getPassportNo())
//                .passportIssueDate(LocalDate.now())
                .passportExpirationDate(memberRequestDto.getPassportExpirationDate())
//                .passportDocId("01")
//                .drivingLicenseNo("123456789")
//                .drivingLicenseDocId("01")
//                .otherIdNo("123456789")
//                .otherDocName("Other Document")
//                .otherIdDocId("01")
//                .photoImageId("01")
                .tinNo(memberRequestDto.getTinNo())
//                .tinDocId("01")
//                .loginId("johndoe")
//                .loginEnabledOn(LocalDateTime.now())
//                .memberPassword("password")
//                .microEnName("Micro Name")
//                .microLegalForm("Micro Legal Form")
                .mfiProgramId(memberRequestDto.getMfiProgramId())
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
                .businessDate(requestDto.getBusinessDate())
                .managementProcessId(requestDto.getManagementProcessId())
                .build();
    }
}
