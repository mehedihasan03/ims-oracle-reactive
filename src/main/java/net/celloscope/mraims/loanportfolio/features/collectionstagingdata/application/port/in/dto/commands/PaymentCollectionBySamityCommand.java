package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.Attendance;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentCollectionBySamityCommand {
    private String managementProcessId;
    private String processId;

    public String mfiId;
    public String loginId;
    public String officeId;
    public String fieldOfficerId;

    public String samityId;
    public String collectionType;
    public List<CollectionData> data;
    public List<Attendance> attendanceList;
}
