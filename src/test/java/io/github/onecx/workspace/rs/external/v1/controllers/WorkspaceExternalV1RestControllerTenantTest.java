package io.github.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceInfoListDTOV1;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceExternalV1RestControllerTenantTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("themeNamesAndResults")
    void getWorkspacesByThemeName(String themeName, int results, String organisation) {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("themeName", themeName)
                .header(APM_HEADER_PARAM, createToken(organisation))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceInfoListDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).hasSize(results);
    }

    private static Stream<Arguments> themeNamesAndResults() {
        return Stream.of(
                arguments("11-111", 3, "org1"),
                arguments("22-222", 0, "org1"), // different tenant so will not find it
                arguments("does-not-exists", 0, "org1"),
                arguments("11-111", 0, "org2"),
                arguments("22-222", 2, "org2"));
    }
}
