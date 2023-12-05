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
class WorkspaceExternalV1RestControllerTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("themeNamesAndResults")
    void getWorkspacesByThemeName(String themeName, int results) {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("themeName", themeName)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceInfoListDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).hasSize(results);
    }

    private static Stream<Arguments> themeNamesAndResults() {
        return Stream.of(
                arguments("11-111", 3),
                arguments("22-222", 2),
                arguments("does-not-exists", 0));
    }
}
