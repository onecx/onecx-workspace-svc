package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void deleteMenuItemByIdTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-13")
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isNotEmpty().hasSize(12);
    }

    @Test
    void deleteAllMenuItemsForWorkspaceTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isEmpty();
    }

    @Test
    void createMenuItemForWorkspaceTest() {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setParentItemId("44-1");
        menuItem.setI18n(Map.of("de", "Test DE Menu", "en", "Test EN Menu"));

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
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
    void addMenuItemBadRequestTest(String key, String parentItemId, String workspaceName) {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey(key);
        menuItem.setDisabled(false);
        menuItem.setParentItemId(parentItemId);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", workspaceName)
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
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-6")
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Portal Child 1");
    }

    @Test
    void getMenuStructureForWorkspaceIdTest() {
        var data = given()
                .when()
                .pathParam("id", "11-111")
                .get("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructureDTO.class);

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
        var data = given()
                .when()
                .pathParam("id", "does-not-exists")
                .get("/tree")
                .then().statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructureDTO.class);

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
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "does-not-exists")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(NOT_FOUND.getStatusCode());

        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "wrong-workspace")
                .pathParam("menuItemId", "44-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(MenuInternalRestController.MenuItemErrorKeys.WORKSPACE_DIFFERENT.name());
        assertThat(error.getDetail()).isEqualTo("Menu item does have different workspace");

        request.parentItemId("44-2");

        error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("44-3")
                .position(1)
                .modificationCount(dto.getModificationCount());

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(OK.getStatusCode());
    }

    @Test
    void updateMenuItemDoesNotUpdateParentTest() {

        var request = new UpdateMenuItemRequestDTO();
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-2");
        request.setModificationCount(0);

        // update menu item
        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());

        request.setModificationCount(updatedData.getModificationCount());

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-333")
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateMenuItemParentSetChildAsParentTest() {

        var request = new UpdateMenuItemRequestDTO();
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-6");
        request.setModificationCount(0);

        // update menu item
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
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
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-2");
        request.setModificationCount(0);

        // update menu item
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
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
                Arguments.of("11-111", "33-2", "55-6"),
                Arguments.of("11-222", "44-6", "does-not-exists"),
                Arguments.of("11-222", "44-6", "55-1"),
                Arguments.of("11-222", "44-6", "33-11"));
    }

    @ParameterizedTest
    @MethodSource("inputParams")
    void updateMenuItemErrors(String workspaceName, String menuItemId, String parentItemId) {
        var request = new UpdateMenuItemRequestDTO();
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId(parentItemId);
        request.setModificationCount(0);

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", workspaceName)
                .pathParam("menuItemId", menuItemId)
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateMenuItemTest() {
        var request = new UpdateMenuItemRequestDTO();
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
                .pathParam("id", "11-222")
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
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .put("{menuItemId}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());

        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
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
        request.setKey("Test menu");
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");
        request.setModificationCount(0);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "not-exists")
                .put("{menuItemId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void uploadMenuStructureNoBodyTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post("/tree")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void uploadMenuStructureNoMenuItemsTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructureDTO();
        menuStructureListDTO.setMenuItems(null);

        var error = given()
                .when()
                .body(menuStructureListDTO)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post("/tree")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");

        menuStructureListDTO = new WorkspaceMenuItemStructureDTO();
        menuStructureListDTO.setMenuItems(new ArrayList<>());

        error = given()
                .when()
                .body(menuStructureListDTO)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post("/tree")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
    }

    @Test
    void uploadMenuStructurePortalDoesNotExistsTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructureDTO();
        var menuItemStructureDTO = new WorkspaceMenuItemDTO();

        menuItemStructureDTO.setKey("Test menu");
        menuItemStructureDTO.setDisabled(false);
        menuItemStructureDTO.setParentItemId("44-1");

        menuStructureListDTO.addMenuItemsItem(menuItemStructureDTO);

        var error = given()
                .when()
                .body(menuStructureListDTO)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "does-not-exists")
                .post("/tree")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");
    }

    @Test
    void uploadMenuStructureTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructureDTO();
        var menuItemStructureDTO = new WorkspaceMenuItemDTO();

        menuItemStructureDTO.setKey("Test menu");
        menuItemStructureDTO.setDisabled(false);
        menuItemStructureDTO.setParentItemId("44-1");
        menuItemStructureDTO.setChildren(new ArrayList<>());

        var menuItemStructureDTO1 = new WorkspaceMenuItemDTO();
        menuItemStructureDTO1.setKey("Sub menu");
        menuItemStructureDTO1.setDisabled(false);
        menuItemStructureDTO1.setParentItemId("44-1");
        menuItemStructureDTO.addChildrenItem(menuItemStructureDTO1);
        menuItemStructureDTO1 = new WorkspaceMenuItemDTO();
        menuItemStructureDTO1.setKey("Sub menu2");
        menuItemStructureDTO1.setDisabled(false);
        menuItemStructureDTO1.setParentItemId("44-1");
        menuItemStructureDTO.addChildrenItem(menuItemStructureDTO1);
        menuItemStructureDTO1 = new WorkspaceMenuItemDTO();
        menuItemStructureDTO1.setKey("Sub menu3");
        menuItemStructureDTO1.setDisabled(false);
        menuItemStructureDTO1.setParentItemId("44-1");
        menuItemStructureDTO.addChildrenItem(menuItemStructureDTO1);

        menuStructureListDTO.addMenuItemsItem(menuItemStructureDTO);

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuStructureListDTO)
                .pathParam("id", "11-222")
                .post("/tree")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var data = given()
                .when()
                .pathParam("id", "11-222")
                .get("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(1);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(4);
    }
}
