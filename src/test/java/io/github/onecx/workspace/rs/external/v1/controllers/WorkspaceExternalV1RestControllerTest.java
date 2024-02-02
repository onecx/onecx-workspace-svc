package io.github.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceDTOV1;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspacePageResultDTOV1;
import gen.io.github.onecx.workspace.rs.external.v1.model.WorkspaceSearchCriteriaDTOV1;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceExternalV1RestControllerTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("criteriaAndResults")
    void searchWorkspacesByCriteria(WorkspaceSearchCriteriaDTOV1 criteriaDTOV1, int results) {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteriaDTOV1)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).hasSize(results);
    }

    @Test
    void getWorkspaceByNameTest() {
        var dto = given()
                .when()
                .pathParam("name", "test01")
                .get("/name/{name}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("test01");
    }

    @Test
    void getALlWorkspaceNamesTest() {
        var names = given()
                .when()
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(names).isNotNull().hasSize(3);
    }

    @Test
    void getALlWorkspacesByProductNameTest() {
        var names = given()
                .when()
                .pathParam("productName", "onecx-core")
                .get("/productName/{productName}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(names).isNotNull();
        assertThat(names).hasSize(2);
        assertThat(names).contains("test01").contains("test02");
    }

    @Test
    void getWorkspaceByNameNotFound() {
        var dto = given()
                .when()
                .pathParam("name", "not-found")
                .get("/name/{name}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        assertThat(dto).isNotNull();
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
                arguments(criteria1, 3),
                arguments(criteria2, 0), // different tenant so will not find it
                arguments(criteria3, 0),
                arguments(emptyCriteria, 3));
    }
}
