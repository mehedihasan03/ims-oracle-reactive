package net.celloscope.mraims.loanportfolio.core.tenantmanagement.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.tenantmanagement.factory.TenantAwareConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.sql.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.*;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Slf4j
@Configuration
@EnableR2dbcRepositories
public class TenantConfig {
    private Set<String> instituteList = new HashSet<>();
    private final Map<String, ConnectionPool> connectionFactories = new HashMap<>();
    private final TenantAwareConnectionFactory instituteConnectionFactory = new TenantAwareConnectionFactory();

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
    @Value("${spring.r2dbc.initial.size}")
    private Integer initialSize;
    @Value("${spring.r2dbc.max.size}")
    private Integer maxSize;
    @Value("${spring.r2dbc.max.idle.time}")
    private Integer maxIdleTime;
    @Value("${spring.r2dbc.default.schema}")
    private String defaultSchema;

    @Bean
    public TenantAwareConnectionFactory connectionFactory() {
        instituteConnectionFactory.setDefaultTargetConnectionFactory(createConnectionFactory(defaultSchema));
        instituteConnectionFactory.setTargetConnectionFactories(connectionFactories());
        return instituteConnectionFactory;
    }

    private Map<String, ConnectionPool> connectionFactories() {
        getSchemaMap()
            .forEach((oid, schemaName) -> {
                log.info("Institute Oid : {} and Schema : {}", oid, schemaName);
                connectionFactories.putIfAbsent(oid, new ConnectionPool(
                    ConnectionPoolConfiguration.builder()
                        .connectionFactory(createConnectionFactory(schemaName))
                        .build()));
            });

        instituteList = connectionFactories.keySet();
        return connectionFactories;
    }

    private ConnectionFactory createConnectionFactory(String schema) {
        return ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(PROTOCOL, "oracle")
                .option(DRIVER, POOLING_DRIVER)
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, username)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .option(Option.valueOf("schema"), schema)
                .option(INITIAL_SIZE, initialSize)
                .option(MAX_SIZE, maxSize)
                .option(MAX_IDLE_TIME, Duration.ofMillis(maxIdleTime))
                .option(Option.valueOf("validationQuery"), "SELECT 1 FROM DUAL")
                .build());
    }

    @Bean
    public TransactionalOperator transactionalOperator(ConnectionFactory connectionFactory) {
        ReactiveTransactionManager transactionManager = new R2dbcTransactionManager(connectionFactory);
        return TransactionalOperator.create(transactionManager);
    }

    public Boolean newInstituteFound() {
        return !instituteList.containsAll(getSchemaMap().keySet());
    }

    private Map<String, String> getSchemaMap() {
        Map<String, String> schemaMap = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(
            "jdbc:oracle:thin:@" + host + ":" + port + "/" + database,
            username,
            password
        )) {
            connection.setSchema(defaultSchema);
            String sql = "SELECT * FROM INSTITUTE";
            try (PreparedStatement p = connection.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String oid = rs.getString("OID");
                    String schemaName = rs.getString("DB_SCHEMA_NAME");
                    schemaMap.putIfAbsent(oid, schemaName);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return schemaMap;
    }

    public void updateConnectionFactories() {
        newInstitutesSchemaMap().forEach((tenantId, schema) -> {
            log.info("New Institute Oid : {} and Schema : {}", tenantId, schema);
            connectionFactories.putIfAbsent(tenantId, new ConnectionPool(
                ConnectionPoolConfiguration.builder()
                    .connectionFactory(createConnectionFactory(schema))
                    .build()));
        });
        instituteConnectionFactory.updateTargetConnectionFactories(connectionFactories);
    }

    public Map<String, String> newInstitutesSchemaMap() {
        Set<String> currentInstitutes = getSchemaMap().keySet();
        currentInstitutes.removeAll(instituteList);
        return getSchemaMap().entrySet().stream()
            .filter(e -> currentInstitutes.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
