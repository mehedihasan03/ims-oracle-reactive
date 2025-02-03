package net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayResponseDTO {
    public String holidayId;
    public String calendarDayId;
    public String officeId;
    public LocalDate holidayDate;
    public String holidayType;
    public String titleEn;
    public String titleBn;
    public String mfiId;
    public String status;
}
