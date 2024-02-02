package org.tkit.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspacePageResultDTOV1;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceExternalV1RestControllerTenantTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("criteriaAndResults")
    void searchWorkspacesByCriteria(WorkspaceSearchCriteriaDTOV1 criteriaDTOV1, int results, String organisation) {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteriaDTOV1)
                .header(APM_HEADER_PARAM, createToken(organisation))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).hasSize(results);
    }

    private static Stream<Arguments> criteriaAndResults() {
        WorkspaceSearchCriteriaDTOV1 criteria1 = new WorkspaceSearchCriteriaDTOV1();
        criteria1.setThemeName("11-111");
        WorkspaceSearchCriteriaDTOV1 criteria2 = new WorkspaceSearchCriteriaDTOV1();
        criteria2.setThemeName("22-222");
        WorkspaceSearchCriteriaDTOV1 criteria3 = new WorkspaceSearchCriteriaDTOV1();
        criteria3.setThemeName("does-not-exists");
        WorkspaceSearchCriteriaDTOV1 emptyCriteria = new WorkspaceSearchCriteriaDTOV1();

        return Stream.of(
                arguments(criteria1, 3, "org1"),
                arguments(criteria2, 0, "org1"), // different tenant so will not find it
                arguments(criteria3, 0, "org1"),
                arguments(emptyCriteria, 3, "org1"));
    }
}
