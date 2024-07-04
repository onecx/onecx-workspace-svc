package org.tkit.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.external.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:read", "ocx-ws:write" })
class WorkspaceExternalV1RestControllerTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("criteriaAndResults")
    void searchWorkspacesByCriteria(WorkspaceSearchCriteriaDTOV1 criteriaDTOV1, int results) {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteriaDTOV1)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream().get(0).getName()).isEqualTo("test01");
    }

    @Test
    void getWorkspaceByNameTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .pathParam("name", "test01")
                .get("{name}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProducts()).isNotNull().hasSize(2).containsExactly("onecx-core", "onecx-apm");
        assertThat(dto.getName()).isEqualTo("test01");
    }

    @Test
    void getALlWorkspacesByProductNameTest() {

        var criteria = new WorkspaceSearchCriteriaDTOV1().productName("onecx-core");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspacePageResultDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(6);
    }

    @Test
    void getWorkspaceByNameNotFound() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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
                arguments(emptyCriteria, 6));
    }

    @Test
    void getProductsForWorkspaceNameTest() {
        // not existing product
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("name", "does-not-exist")
                .get("/{name}/load")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // existing product
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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

    @Test
    void getWorkspaceByUrlTest() {

        GetWorkspaceByUrlRequestDTOV1 requestDTOV1 = new GetWorkspaceByUrlRequestDTOV1();
        requestDTOV1.setUrl("does-not-exist-url");
        // not existing workspace
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/byUrl")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // existing workspace
        requestDTOV1.setUrl("/company2/admin/my/url");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/byUrl")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getCompanyName()).isNotNull().isNotEmpty().isEqualTo("Company2");

        // another test to match /de base path
        requestDTOV1.setUrl("/de/test/some/strange");
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/byUrl")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getCompanyName()).isNotNull().isNotEmpty().isEqualTo("Company44");

        // test with a http protocol
        requestDTOV1.setUrl("https://my.domain.com/de/test/some/strange");
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/byUrl")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getCompanyName()).isNotNull().isNotEmpty().isEqualTo("Company44");

        // strange protocol
        requestDTOV1.setUrl("asd://");
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/byUrl")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void loadWorkspaceByUrlTest() {

        var requestDTOV1 = new WorkspaceLoadRequestDTOV1().path("does-not-exist-url");

        // not existing workspace
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/load")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // existing workspace
        requestDTOV1.setPath("/company1/admin/my/url");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/load")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceWrapperDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("test01");
        assertThat(dto.getSlots()).isNotNull().hasSize(3);
        assertThat(dto.getSlots().get(0)).isNotNull();
        var s = dto.getSlots().stream().filter(x -> "slot1".equals(x.getName())).findFirst().orElse(null);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualTo("slot1");
        assertThat(s.getComponents()).isNotNull().hasSize(3);
        assertThat(s.getComponents().stream().filter(x -> "c1".equals(x.getName())).findFirst()).isPresent();
        assertThat(s.getComponents().stream().filter(x -> "c2".equals(x.getName())).findFirst()).isPresent();
        assertThat(s.getComponents().stream().filter(x -> "c3".equals(x.getName())).findFirst()).isPresent();

        requestDTOV1.setPath("/company2/admin/my/url");
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/load")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceWrapperDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getSlots()).isNotNull().hasSize(1);
        assertThat(dto.getName()).isNotNull().isNotEmpty().isEqualTo("test02");

        // another test to match /de base path
        requestDTOV1.setPath("/de/test/some/strange");
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/load")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceWrapperDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("test44");

        // strange protocol
        requestDTOV1.setPath("asd://");
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOV1)
                .post("/load")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

}
