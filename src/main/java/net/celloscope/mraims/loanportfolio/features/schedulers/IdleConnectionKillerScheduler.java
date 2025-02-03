package net.celloscope.mraims.loanportfolio.features.schedulers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;

@Slf4j
@Component
public class IdleConnectionKillerScheduler {

    @Value("${spring.r2dbc.host}")
    private String host;
    @Value("${spring.r2dbc.port}")
    private Integer port;
    @Value("${spring.r2dbc.username}")
    private String username;
    @Value("${spring.r2dbc.password}")
    private String password;
    @Value("${spring.r2dbc.database}")
    private String database;

    @Value("${kill-idle-connections-scheduler.enabled}")
    private String isKillIdleConnectionsSchedulerEnabled;

    @Scheduled(fixedRate = 3600000)
    public void killIdleConnections() {
        if (!Boolean.parseBoolean(isKillIdleConnectionsSchedulerEnabled)) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(
            "jdbc:postgresql://" + host + ":" + port + "/",
            username,
            password
        )) {
            String sql =    "SELECT \n" +
                            "    pg_terminate_backend(pid) \n" +
                            "FROM \n" +
                            "    pg_stat_activity \n" +
                            "WHERE \n" +
                            "    pid <> pg_backend_pid()\n" +
                            "    AND datname = '"+ database + "'\n" +
                            "    AND state = 'idle'\n" +
                            "    AND state_change < current_timestamp - INTERVAL '10 minutes'\n" +
                            "    ;";
            try (PreparedStatement p = connection.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
                log.info("Killed idle connections");
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
