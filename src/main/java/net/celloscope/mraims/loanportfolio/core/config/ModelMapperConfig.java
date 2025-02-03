package net.celloscope.mraims.loanportfolio.core.config;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.CollectionStagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.InstallmentDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.Installment;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        
        /*Converter<LocalDate, LocalDate> LOCAL_DATE_CONVERTER = ctx -> ctx.getSource() != null
                ? ctx.getSource()
                : null;
        TypeMap<Installment, InstallmentDTO> barToFooMapping = modelMapper.createTypeMap(Installment.class, InstallmentDTO.class);
        barToFooMapping.addMappings(mapping -> mapping.using(LOCAL_DATE_CONVERTER).map(Installment::getInstallmentDate, InstallmentDTO::setInstallmentDate));
        TypeMap<InstallmentDTO, Installment> fooToBarMapping = modelMapper.createTypeMap(InstallmentDTO.class, Installment.class);
        fooToBarMapping.addMappings(mapping -> mapping.using(LOCAL_DATE_CONVERTER).map(InstallmentDTO::getInstallmentDate, Installment::setInstallmentDate));
*/
        return modelMapper;
    }

}
