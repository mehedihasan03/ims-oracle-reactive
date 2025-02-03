/*
package net.celloscope.mraims.loanportfolio.sajal.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.StagingDataGridViewQueryDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service.CollectionStagingDataQueryService;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionStagingDataGridViewResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class CollectionStagingDataViewServiceIntegrationTest {
    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:11.7-alpine")
                    .withDatabaseName("mra")
                    .withUsername("mra")
                    .withPassword("mra")
                    .withInitScript("sqlfiles/view.sql")
                    .withReuse(true);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                + postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort()
                + "/" + postgreSQLContainer.getDatabaseName());
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
    }


    @Autowired
    CollectionStagingDataQueryService service;

    @Test
    void contextLoading() {
        Assertions.assertNotNull(CollectionStagingDataQueryService.class);
    }

    @Test
    void service_test() {

        Mono<CollectionStagingDataGridViewResponse> response = service.gridViewStagingDataOfAFieldOfficer(requestBodyBuild());

        StepVerifier
                .create(response)
                .expectSubscription()
                .consumeNextWith(collectionStagingDataGridViewResponse ->
                        System.out.println("response ++++ " + collectionStagingDataGridViewResponse)
                )
                .verifyComplete();

    }

    private StagingDataGridViewQueryDto requestBodyBuild() {
        return StagingDataGridViewQueryDto
                .builder()
                .officeId("12345")
                .officeId("EMP-0007")
                .mfiId("5678")
                .loginId("98765")
                .build();
    }

}*/
