package net.celloscope.mraims.loanportfolio.features.migration.components.person;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class MigrationPersonService {

    private final MigrationPersonRepository migrationPersonRepository;

    public Mono<Person> save(MigrationMemberRequestDto migrationMemberRequestDto, MigrationRequestDto requestDto) {
        return migrationPersonRepository.findByPersonId(getPersonId(migrationMemberRequestDto))
                .doOnNext(person -> log.info("Person Found for Person Id: {}, Person Name: {}", person.getPersonId(), person.getPersonNameEn()))
                .switchIfEmpty(migrationPersonRepository.save(buildPerson(migrationMemberRequestDto, requestDto.getMfiId(), requestDto.getLoginId())))
                .doOnNext(person -> log.info("Person with Person Id: {}", person.getPersonId()))
                .doOnError(throwable -> log.error("Error occurred while saving Person: {}", throwable.getMessage()));
    }

    private String getPersonId(MigrationMemberRequestDto memberRequestDto) {
        return MigrationEnums.PERSON_SHORT_NAME.getValue() + "-" + memberRequestDto.getCompanyMemberId();
    }

    private Person buildPerson(MigrationMemberRequestDto memberRequestDto, String mfiId, String loginId) {
        return Person.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.PERSON_SHORT_NAME.getValue() + "-" + UUID.randomUUID())   // This is the primary key
                .personId(MigrationEnums.PERSON_SHORT_NAME.getValue() + "-" + memberRequestDto.getCompanyMemberId())  // *Required
//                .passbookNumber("123456")
                .personNameEn(memberRequestDto.getMemberName()) // *Required
                .personNameBn(memberRequestDto.getMemberName())
                .fatherNameEn("Father")
                .fatherNameBn("ফাদার")
                .motherNameEn("Mother")
                .motherNameBn("মাদার")
//                .spouseNameEn("Spouse Doe")
//                .spouseNameBn("স্পাউস ডো")
//                .spouseContactNo("0123456789")
                .dateOfBirth(LocalDate.of(1980, 1, 1)) // *Required
                .ageOnAppDate("44")
                .nationality("Bangladeshi")
                .gender("Male")
                .mobile("0123456789")
                .email("email@gmail.com")
                .resDivisionId(memberRequestDto.getResDivisionId())
                .resDistrictId(memberRequestDto.getResDistrictId())
                .resUpazilaId(memberRequestDto.getResUpazilaId())
//                .resUnionId(memberRequestDto.getResUnionId())
//                .resWardVillageStreet("Street 1")
//                .resPostOfficeId("01")
//                .resPostalCode("1000")
                .resAddressLine1(memberRequestDto.getResAddressLine1())
//                .resAddressLine2("Address Line 2")
                .perDivisionId(memberRequestDto.getPerDivisionId())
                .perDistrictId(memberRequestDto.getPerDistrictId())
                .perUpazilaId(memberRequestDto.getPerUpazilaId())
                .perUnionId(memberRequestDto.getPerUnionId())
//                .perWardVillageStreet("Street 1")
//                .perPostOfficeId("01")
//                .perPostalCode("1000")
                .perAddressLine1(memberRequestDto.getPerAddressLine1())
//                .perAddressLine2("Address Line 2")
                .identificationType("NID")
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
                .mfiId(mfiId)    // *Required
                .migratedOn(LocalDateTime.now())
                .migratedBy(loginId)
                .status(MigrationEnums.STATUS_ACTIVE.getValue())
                .createdBy(loginId)
                .createdOn(LocalDateTime.now()) // *Required
//                .updatedBy("User")
//                .updatedOn(LocalDateTime.now())
                .build();
    }

}
