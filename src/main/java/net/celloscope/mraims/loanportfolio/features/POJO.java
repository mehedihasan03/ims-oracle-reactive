package net.celloscope.mraims.loanportfolio.features;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.config.ModelMapperConfig;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity.MetaPropertyEntity;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.LoanCalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.InterestPostingResponse;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.InstallmentCalculationDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookCalculationDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cglib.core.Local;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class POJO {

    public static void main(String[] args) {
        System.out.println("Hello World");
    }

}
