package net.celloscope.mraims.loanportfolio.features.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.util.CurrentTenantIdHolder;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerHelperUtils {

    @Value("${spring.r2dbc.host}")
    private String host;
    @Value("${spring.r2dbc.port}")
    private Integer port;
    @Value("${spring.r2dbc.username}")
    private String username;
    @Value("${spring.r2dbc.password}")
    private String password;
    @Value("${spring.r2dbc.default.schema}")
    private String defaultSchema;

    private final CommonRepository commonRepository;

    public Flux<String> getActiveOfficeIdsForInstitute() {
        return Flux.fromIterable(getInstituteOidList())
            .doOnNext(oid -> log.info("Processing for institute: {}", oid))
            .flatMap(oid -> Mono.just(oid)
                .contextWrite(CurrentTenantIdHolder.withId(oid))
                .flatMapMany(v -> commonRepository.getActiveOfficeIds())
                .doOnNext(officeId -> log.info("Processing for institute {} for office: {}", oid, officeId))
            );
    }

    public List<String> getInstituteOidList() {
        List<String> oidList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://" + host + ":" + port + "/",
            username,
            password
        )) {
            connection.setSchema(defaultSchema);
            String sql = "select * from institute";
            try (PreparedStatement p = connection.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String oid = rs.getString("oid");
                    oidList.add(oid);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        oidList.remove("MRA-IMS-MFI-Oid-MRA");
        return oidList;
    }
}
