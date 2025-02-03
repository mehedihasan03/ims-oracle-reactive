package net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaPropertyResponseDTO {

    private String propertyId;

    private String description;

    private String status;

    private String parameters;
}
