package org.tkit.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.*;
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
    void searchWorkspacesByBasePathCriteria() {
        WorkspaceSearchCriteriaDTOV1 criteriaDTOV1 = new WorkspaceSearchCriteriaDTOV1();
        criteriaDTOV1.setBaseUrl("/company1");
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteriaDTOV1)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat("test01").isEqualTo(dto.getStream().get(0).getName());
    }

    @Test
    void getWorkspaceByNameTest() {
        var dto = given()
                .when()
                .pathParam("name", "test01")
                .get("{name}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("test01");
    }

    @Test
    void getALlWorkspacesByProductNameTest() {

        var criteria = new WorkspaceSearchCriteriaDTOV1().productName("onecx-core");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(3);
    }

    @Test
    void getWorkspaceByNameNotFound() {
        var dto = given()
                .when()
                .pathParam("name", "not-found")
                .get("{name}")
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

    @Test
    void getProductsForWorkspaceNameTest() {
        // not existing product
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", "does-not-exist")
                .get("/{name}/load")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // existing product
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", "test01")
                .get("/{name}/load")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceLoadDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).isNotNull().isNotEmpty().hasSize(2);
        assertThat(dto.getProducts().get(0).getProductName()).isNotEmpty();
        assertThat(dto.getProducts().get(1).getProductName()).isNotEmpty();
    }
}
