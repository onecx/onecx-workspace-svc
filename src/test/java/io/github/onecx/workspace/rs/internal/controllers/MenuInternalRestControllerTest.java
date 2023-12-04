package io.github.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

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
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class MenuInternalRestControllerTest extends AbstractTest {

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

        assertThat(dto).isNotNull().isNotEmpty();
        assertThat(dto).hasSize(13);
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

        assertThat(dto).isNotNull().isNotEmpty();
        assertThat(dto).hasSize(12);
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
        assertThat(dto).hasSize(0);
    }

    @Test
    void createMenuItemForWorkspaceTest() {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));
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

    @Test
    void addMenuItemForPortalDuplicateKeyTest() {
        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("PORTAL_MAIN_MENU");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));
        menuItem.setParentItemId("44-1");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Add menu item to the portal with without parent menu item")
    void addMenuItemForPortalWithoutParentTest() {

        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));

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
    @DisplayName("Add menu item to the portal with wrong menu parent from another portal")
    void addMenuItemForPortalParentFromAnotherPortalTest() {

        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));
        menuItem.setParentItemId("33-6");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Add menu item to the portal with wrong menu parent id")
    void addMenuItemForPortalWrongMenuParentIdTest() {

        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));
        menuItem.setParentItemId("does-not-exists");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    @DisplayName("Add menu item to the portal which does not exists")
    void addMenuItemForPortalWrongPortalIdTest() {

        CreateMenuItemDTO menuItem = new CreateMenuItemDTO();
        menuItem.setName("menu");
        menuItem.setKey("test01_menu");
        menuItem.setDisabled(false);
        menuItem.setRoles(List.of("Role1"));
        menuItem.setParentItemId("does-not-exists");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "does-not-exists")
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
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
                .extract().body().as(WorkspaceMenuItemStructrueDTO.class);

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
                .extract().body().as(WorkspaceMenuItemStructrueDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).isEmpty();
    }

    @Test
    void bulkPatchMenuItemsNoBodyTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .patch()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void bulkPatchMenuItemsEmptyListTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of())
                .pathParam("id", "11-222")
                .patch()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void patchMenuItemsTest() {
        var newRolesMenuItem = List.of("Role2");

        var menuItemDetailsDTO = new MenuItemDTO();
        menuItemDetailsDTO.setId("44-1");
        menuItemDetailsDTO.setName("Test menu 44-1");
        menuItemDetailsDTO.setDisabled(false);
        menuItemDetailsDTO.setRoles(newRolesMenuItem);

        var menuItemDetailsDTO1 = new MenuItemDTO();
        menuItemDetailsDTO1.setId("44-2");
        menuItemDetailsDTO1.setParentItemId("44-5");
        menuItemDetailsDTO1.setName("Test menu 44-2");
        menuItemDetailsDTO1.setDisabled(false);
        menuItemDetailsDTO1.setRoles(newRolesMenuItem);

        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of(menuItemDetailsDTO, menuItemDetailsDTO1))
                .pathParam("id", "11-222")
                .patch()
                .then().statusCode(OK.getStatusCode())
                .extract()
                .as(new TypeRef<List<MenuItemDTO>>() {
                });

        assertThat(updatedData).isNotNull().hasSize(2);

        updatedData.forEach(e -> {
            switch (e.getName()) {
                case "Test menu 44-1":
                case "Test menu 44-2":
                    assertThat(e.getWorkspaceName()).isEqualTo("test02");
                    break;
                default:
                    assertThat(e.getWorkspaceName()).isNull();
            }
        });
    }

    @Test
    void bulkPatchMenuItemsNewMenuItemTest() {

        var menuItemDetailsDTO = new MenuItemDTO();
        menuItemDetailsDTO.setId("44-1");
        menuItemDetailsDTO.setName("Test menu 44-1");
        menuItemDetailsDTO.setDisabled(false);
        menuItemDetailsDTO.setRoles(List.of("Role2"));

        var menuItemDetailsDTO1 = new MenuItemDTO();
        menuItemDetailsDTO1.setId("does-not-exists");
        menuItemDetailsDTO1.setParentItemId("44-5");
        menuItemDetailsDTO1.setName("Test menu 44-2");
        menuItemDetailsDTO1.setDisabled(false);
        menuItemDetailsDTO1.setRoles(List.of("Role2"));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of(menuItemDetailsDTO, menuItemDetailsDTO1))
                .pathParam("id", "11-222")
                .patch()
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void bulkPatchMenuItemsParentToParentTest() {

        var menuItemDetailsDTO1 = new MenuItemDTO();
        menuItemDetailsDTO1.setId("44-5");
        menuItemDetailsDTO1.setParentItemId("44-5");
        menuItemDetailsDTO1.setName("Test menu 44-2");
        menuItemDetailsDTO1.setDisabled(false);
        menuItemDetailsDTO1.setRoles(List.of("Role2"));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of(menuItemDetailsDTO1))
                .pathParam("id", "11-222")
                .patch()
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void patchMenuItemDoesNotUpdateParentTest() {

        var request = new MenuItemDTO();
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId("44-2");

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
        assertThat(updatedData.getWorkspaceName()).isEqualTo("test02");
    }

    static Stream<Arguments> inputParams() {
        return Stream.of(
                Arguments.of("22-111", "55-4", "55-6"),
                Arguments.of("11-222", "44-6", "does-not-exists"),
                Arguments.of("11-222", "44-6", "55-1"));
    }

    @ParameterizedTest
    @MethodSource("inputParams")
    void patchMenuItemErrors(String portalId, String menuItemId, String parentItemId) {
        var request = new MenuItemDTO();
        request.setKey("Test menu");
        request.setDisabled(false);
        request.setParentItemId(parentItemId);

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", portalId)
                .pathParam("menuItemId", menuItemId)
                .put("{menuItemId}")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void updateMenuItemTest() {
        var request = new MenuItemDTO();
        request.setKey("Test menu");
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");

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
        assertThat(updatedData.getWorkspaceName()).isEqualTo("test02");

        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .get("{menuItemId}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(updatedData.getKey()).isEqualTo(request.getKey());
        assertThat(updatedData.getWorkspaceName()).isEqualTo("test02");
        assertThat(updatedData.getDescription()).isEqualTo(request.getDescription());
        assertThat(dto.getName()).isEqualTo("Portal Child 1");
    }

    @Test
    void updateMenuItemNotExistsTest() {
        var request = new MenuItemDTO();
        request.setKey("Test menu");
        request.setDescription("New test menu description");
        request.setDisabled(false);
        request.setParentItemId("44-1");

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
                .post("/tree/upload")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    @Test
    void uploadMenuStructureNoMenuItemsTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructrueDTO();
        menuStructureListDTO.setMenuItems(null);

        var error = given()
                .when()
                .body(menuStructureListDTO)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post("/tree/upload")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("MENU_ITEMS_NULL");

        menuStructureListDTO = new WorkspaceMenuItemStructrueDTO();
        menuStructureListDTO.setMenuItems(new ArrayList<>());

        error = given()
                .when()
                .body(menuStructureListDTO)
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .post("/tree/upload")
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("MENU_ITEMS_NULL");
    }

    @Test
    void uploadMenuStructurePortalDoesNotExistsTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructrueDTO();
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
                .post("/tree/upload")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");
    }

    @Test
    void uploadMenuStructureTest() {

        var menuStructureListDTO = new WorkspaceMenuItemStructrueDTO();
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
                .post("/tree/upload")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var data = given()
                .when()
                .pathParam("id", "11-222")
                .get("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructrueDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(1);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(4);
    }
}
