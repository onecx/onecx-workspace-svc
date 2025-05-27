package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:all", "ocx-ws:read", "ocx-ws:write", "ocx-ws:delete" })
class MenuInternalRestControllerTenantTest extends AbstractTest {

    @Test
    void getMenuItemsForWorkspaceIdTest() {

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-222");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(6);

        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org3"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isEmpty();
    }

    @Test
    void deleteMenuItemByIdTest() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-13")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-222");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(6);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-13")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(6);
    }

    @Test
    void deleteAllMenuItemsForWorkspaceTest() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .delete("/workspace/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-222");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty();

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("/workspace/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isEmpty();
    }

    @Test
    void createMenuItemForWorkspaceTest() {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setWorkspaceId("11-222");
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setParentItemId("44-1");
        menuItem.setI18n(Map.of("de", "Test DE Menu", "en", "Test EN Menu"));

        var error = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org3"))
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");

        var uri = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(menuItem)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        assertThat(uri).isNotNull();

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get(uri)
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(menuItem.getName());
        assertThat(dto.getDescription()).isEqualTo(menuItem.getDescription());
    }

    @Test
    void getMenuItemByIdTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-6")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Portal Child 1");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-6")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("{menuItemId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

    }

    @Test
    void getMenuStructureForWorkspaceIdTest() {

        var criteria = new MenuStructureSearchCriteriaDTO()
                .workspaceId("11-222");

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .body(criteria)
                .post("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(MenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).isEmpty();

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(MenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(5);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(6);
    }

    private int countMenuItems(Collection<WorkspaceMenuItemDTO> menuItemDTOS) {
        int count = 0;
        for (WorkspaceMenuItemDTO item : menuItemDTOS) {
            count++;
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                count += countMenuItems(item.getChildren());
            }
        }

        return count;
    }

    @Test
    void updateMenuItemTest() {
        var request = new UpdateMenuItemRequestDTO();
        request.setKey("Test menu");
        request.position(0);
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");
        request.setModificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .put("{menuItemId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update menu item
        var updatedData = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .put("{menuItemId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());
        assertThat(updatedData.getDescription()).isEqualTo(request.getDescription());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "44-6")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{menuItemId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());
        assertThat(updatedData.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getName()).isEqualTo("Portal Child 1");
    }

}
