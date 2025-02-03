package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldName {
    private List<String> fieldNames;
}
