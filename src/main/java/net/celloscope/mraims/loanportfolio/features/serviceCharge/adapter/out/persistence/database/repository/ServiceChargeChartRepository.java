package net.celloscope.mraims.loanportfolio.features.serviceCharge.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.serviceCharge.adapter.out.persistence.database.entity.ServiceChargeChartEntity;
import net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto.CombinedDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
@Repository
public interface ServiceChargeChartRepository extends ReactiveCrudRepository<ServiceChargeChartEntity, String> {
    Mono<ServiceChargeChartEntity> findByLoanProductId(String loanProductId);

    @Query("""
    select s.samity_day, lp.repayment_frequency, scc.service_charge_rate , scc.service_charge_rate_freq, lp.interest_calc_method from template.loan_account la
    INNER JOIN template.loan_product lp
    ON la.loan_product_id = lp.loan_product_id
    INNER JOIN template.mem_smt_off_pri_map msopm
    ON la.member_id = msopm .member_id
    INNER JOIN template.samity s
    ON msopm.samity_id = s.samity_id
    INNER JOIN template.service_charge_chart scc
    ON la.loan_product_id = scc.loan_product_id
    WHERE la.loan_account_id  = :loanAccountId
    AND msopm.status = 'Active';
    """)
    Mono<CombinedDTO> getCombinedDTO(String loanAccountId);

}
