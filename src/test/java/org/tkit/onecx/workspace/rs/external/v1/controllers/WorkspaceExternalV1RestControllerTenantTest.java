package org.tkit.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspacePageResultDTOV1;
import gen.org.tkit.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:read", "ocx-ws:write" })
class WorkspaceExternalV1RestControllerTenantTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("criteriaAndResults")
    void searchWorkspacesByCriteria(WorkspaceSearchCriteriaDTOV1 criteriaDTOV1, int results, String organisation) {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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
        return Stream.of(
                arguments(new WorkspaceSearchCriteriaDTOV1().themeName("11-111"), 8, "org1"),
                arguments(new WorkspaceSearchCriteriaDTOV1().themeName("22-222"), 0, "org1"), // different tenant so will not find it
                arguments(new WorkspaceSearchCriteriaDTOV1().themeName("does-not-exists"), 0, "org1"),
                arguments(new WorkspaceSearchCriteriaDTOV1(), 11, "org1"));
    }
}
