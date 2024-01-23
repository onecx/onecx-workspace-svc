package io.github.onecx.workspace.rs.exim.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.exim.v1.model.*;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ExportImportRestControllerV1.class)
@WithDBData(value = "data/testdata-exim.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceEximV1RestControllerTest extends AbstractTest {

    @Test
    void exportWorkspaceTest() {
        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("test01");
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces().get("test01").getWorkspaceName()).isEqualTo("test01");
    }

    @Test
    void exportWorkspaceNotFoundTest() {
        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("12345");
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        assertThat(dto).isNotNull();
    }

    @Test
    void importWorkspaceTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1();
        workspace.setBaseUrl("/someurl");
        workspace.setWorkspaceName("testWorkspace");
        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("testWorkspace", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("testWorkspace", ImportResponseStatusDTOV1.CREATED);
    }

    @Test
    void importWorkspaceAlreadyExistsTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1();
        workspace.setBaseUrl("/company01");
        workspace.setWorkspaceName("test01");
        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("test01", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("test01", ImportResponseStatusDTOV1.SKIP);
    }

    @Test
    void importWorkspaceNoBodyTest() {
        var importResponse = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post("/import")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(EximProblemDetailResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
    }

    @Test
    void exportMenuByWorkspaceIdTest() {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("/test01/menu/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMenu().getMenuItems().get(0).getKey()).isEqualTo("PORTAL_MAIN_MENU");
    }

    @Test
    void exportMenuByWorkspaceIdNotFoundTest() {
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("/test05/menu/export")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        assertThat(dto).isNotNull();
    }

    @Test
    void importMenuByWorkspaceIdTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setKey("testKey");
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test02/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(ImportResponseStatusDTOV1.CREATED);
    }

    @Test
    void importMenuByWorkspaceIdUpdateExistingMenuTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setKey("testKey");
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);
        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test01/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(ImportResponseStatusDTOV1.UPDATE);
    }

    @Test
    void importMenuToNonExistingWorkspaceTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setKey("testKey");
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);
        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/111111/menu/import")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(EximProblemDetailResponseDTOV1.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("WORKSPACE_DOES_NOT_EXIST");
    }
}
