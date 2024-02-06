package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.mapper.ObjectMapperType;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class MenuInternalRestControllerTenantTest extends AbstractTest {

    @Test
    void getMenuItemsForWorkspaceIdTest() {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isNotEmpty().hasSize(13);

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isEmpty();
    }

    @Test
    void deleteMenuItemByIdTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-13")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isNotEmpty().hasSize(13);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-13")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("{menuItemId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .header(APM_HEADER_PARAM, createToken("org2"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(List.class);

        assertThat(dto).isNotNull().isNotEmpty();

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete()
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
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

        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .body(menuItem)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(menuItem)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().header(HttpHeaders.LOCATION);

        assertThat(uri).isNotNull();

        var dto = given().when()
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
        var dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-6")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Portal Child 1");

        dto = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .pathParam("menuItemId", "33-6")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("{menuItemId}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(MenuItemDTO.class, ObjectMapperType.GSON);
        assertThat(dto).isNull();
    }

    @Test
    void getMenuStructureForWorkspaceIdTest() {
        var data = given()
                .when()
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructrueDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).isEmpty();

        data = given()
                .when()
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
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
    void bulkPatchMenuItemsEmptyListTest() {
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of())
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .patch()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void patchMenuItemsTest() {
        var menuItemDetailsDTO = new MenuItemDTO();
        menuItemDetailsDTO.setId("44-1");
        menuItemDetailsDTO.setName("Test menu 44-1");
        menuItemDetailsDTO.setDisabled(false);
        menuItemDetailsDTO.setModificationCount(0);

        var menuItemDetailsDTO1 = new MenuItemDTO();
        menuItemDetailsDTO1.setId("44-2");
        menuItemDetailsDTO1.setParentItemId("44-5");
        menuItemDetailsDTO1.setName("Test menu 44-2");
        menuItemDetailsDTO1.setDisabled(false);
        menuItemDetailsDTO1.setModificationCount(0);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of(menuItemDetailsDTO, menuItemDetailsDTO1))
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .patch()
                .then().statusCode(NOT_FOUND.getStatusCode());

        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(List.of(menuItemDetailsDTO, menuItemDetailsDTO1))
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
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
    void updateMenuItemTest() {
        var request = new MenuItemDTO();
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
                .pathParam("menuItemId", "44-6")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .put("{menuItemId}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update menu item
        var updatedData = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "11-222")
                .pathParam("menuItemId", "44-6")
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .header(APM_HEADER_PARAM, createToken("org1"))
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

        //update menu structure with another tenant
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuStructureListDTO)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .post("/tree/upload")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");

        // update menu item
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(menuStructureListDTO)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post("/tree/upload")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var data = given()
                .when()
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(WorkspaceMenuItemStructrueDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(1);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(4);
    }
}
