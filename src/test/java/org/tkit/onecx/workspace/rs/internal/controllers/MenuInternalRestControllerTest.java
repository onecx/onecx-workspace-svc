package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class MenuInternalRestControllerTest extends AbstractTest {

    @Test
    void getMenuItemsForWorkspaceIdTest() {
        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(13);

        criteria.setWorkspaceId("       ");
        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(19);

        criteria.setWorkspaceId(null);
        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(19);
    }

    @Test
    void deleteMenuItemByIdTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-13")
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(12);
    }

    @Test
    void deleteMenuItemByIdWhenChildrenExistTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-1")
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(7);
    }

    @Test
    void deleteAllMenuItemsForWorkspaceTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .delete("/workspace/{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
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
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO().workspaceId("11-222");
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setParentItemId("44-1");
        menuItem.setI18n(Map.of("de", "Test DE Menu", "en", "Test EN Menu"));

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuItem)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        assertThat(uri).isNotNull();

        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .get(uri)
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(menuItem.getName());
        assertThat(dto.getDescription()).isEqualTo(menuItem.getDescription());
    }

    @ParameterizedTest
    @MethodSource("badRequestArguments")
    void addMenuItemBadRequestTest(String key, String parentItemId, String workspaceId) {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO().workspaceId(workspaceId);
        menuItem.setName("menu");
        menuItem.setKey(key);
        menuItem.setDisabled(false);
        menuItem.setParentItemId(parentItemId);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    private static Stream<Arguments> badRequestArguments() {
        return Stream.of(
                arguments("PORTAL_MAIN_MENU", "44-1", "11-222"),
                arguments("test01_menu", "33-6", "11-222"),
                arguments("test01_menu", "does-not-exists", "11-222"),
                arguments("test01_menu", "does-not-exists", "does-not-exists"));
    }

    @Test
    @DisplayName("Add menu item to the portal with without parent menu item")
    void addMenuItemForPortalWithoutParentTest() {

        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setWorkspaceId("11-222");
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuItem)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        assertThat(uri).isNotNull();

        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .get(uri)
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo(menuItem.getName());
        assertThat(dto.getDescription()).isEqualTo(menuItem.getDescription());
    }

    @Test
    void getMenuItemByIdTest() {
        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "33-6")
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Portal Child 1");
    }

    @Test
    void getMenuStructureForWorkspaceIdTest() {
        var criteria = new MenuStructureSearchCriteriaDTO().workspaceId("11-111");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(MenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(5);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(13);
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
    void getMenuStructureForPortalIdDoesNotExistsTest() {
        var criteria = new MenuStructureSearchCriteriaDTO().workspaceId("does-not-exists");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/tree")
                .then().statusCode(OK.getStatusCode())
                .extract().body().as(MenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).isEmpty();
    }

    @Test
    void updateMenuItemParentErrorTest() {
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("44-6")
                .position(1)
                .modificationCount(0);

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "does-not-exists")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(NOT_FOUND.getStatusCode());

        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(MenuInternalRestController.MenuItemErrorKeys.CYCLE_DEPENDENCY.name());
        assertThat(error.getDetail()).isEqualTo(
                "One of the items try to set one of its children to the new parent. Cycle dependency can not be created in tree structure");

        request.parentItemId("44-2");

        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode())
                .isEqualTo(MenuInternalRestController.MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM.name());
        assertThat(error.getDetail()).isEqualTo("Menu Item 44-2 id and parentItem id are the same");

        request.parentItemId("does-not-exists");
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode())
                .isEqualTo(MenuInternalRestController.MenuItemErrorKeys.PARENT_MENU_DOES_NOT_EXIST.name());
        assertThat(error.getDetail()).isEqualTo("Parent menu item does not exist");

        request.parentItemId("33-13");
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(MenuInternalRestController.MenuItemErrorKeys.WORKSPACE_DIFFERENT.name());
        assertThat(error.getDetail()).isEqualTo("Menu item does have different workspace");

        // update menu item
        request.parentItemId("44-6");
        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(MenuInternalRestController.MenuItemErrorKeys.CYCLE_DEPENDENCY.name());
    }

    @Test
    void updateMenuItemParentTest() {
        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "44-6")
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("44-3")
                .position(1)
                .modificationCount(dto.getModificationCount());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        request = new UpdateMenuItemParentRequestDTO()
                .parentItemId(null)
                .position(1)
                .modificationCount(dto.getModificationCount());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(OK.getStatusCode());
    }

    @Test
    void updateMenuItemDoesNotUpdateParentTest() {

        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-2");
        request.setModificationCount(0);

        // update menu item
        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());

        request.setParentItemId(null);
        request.setModificationCount(updatedData.getModificationCount());

        // update menu item
        updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());

        request.setModificationCount(updatedData.getModificationCount());

    }

    @Test
    void updateMenuItemParentSetChildAsParentTest() {

        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-6");
        request.setModificationCount(0);

        // update menu item
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(MenuInternalRestController.MenuItemErrorKeys.CYCLE_DEPENDENCY.name());
    }

    @Test
    void updateMenuItemParentSetToMenuItemTest() {

        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-2");
        request.setModificationCount(0);

        // update menu item
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode())
                .isEqualTo(MenuInternalRestController.MenuItemErrorKeys.PARENT_MENU_SAME_AS_MENU_ITEM.name());
    }

    static Stream<Arguments> inputParams() {
        return Stream.of(
                Arguments.of("33-2", "55-6"),
                Arguments.of("44-6", "does-not-exists"),
                Arguments.of("44-6", "55-1"),
                Arguments.of("44-6", "33-11"));
    }

    @ParameterizedTest
    @MethodSource("inputParams")
    void updateMenuItemErrors(String menuItemId, String parentItemId) {
        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId(parentItemId);
        request.setModificationCount(0);

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", menuItemId)
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateMenuItemTest() {
        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");
        request.setModificationCount(0);

        // update menu item
        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());
        assertThat(updatedData.getDescription()).isEqualTo(request.getDescription());

        //Update second time and expect a BAD REQUEST because of wrong modificationCount
        request.setModificationCount(-1);
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("menuItemId", "44-6")
                .get("{menuItemId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(dto.getKey());
        assertThat(updatedData.getDescription()).isEqualTo(dto.getDescription());
        assertThat(dto.getName()).isEqualTo("Portal Child 1");
    }

    @Test
    void updateMenuItemNotExistsTest() {
        var request = new UpdateMenuItemRequestDTO();
        request.position(1);
        request.setKey("Test menu");
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");
        request.setModificationCount(0);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "not-exists")
                .put("{menuItemId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

}
