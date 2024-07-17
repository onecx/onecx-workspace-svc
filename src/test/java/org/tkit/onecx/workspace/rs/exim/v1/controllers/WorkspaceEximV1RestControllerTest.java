package org.tkit.onecx.workspace.rs.exim.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.tkit.quarkus.security.test.SecurityTestUtils.getKeycloakClientToken;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.exim.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ExportImportRestControllerV1.class)
@WithDBData(value = "data/testdata-exim.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:read", "ocx-ws:write" })
class WorkspaceEximV1RestControllerTest extends AbstractTest {

    @Test
    void exportWorkspaceTest() {
        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("test01").addNamesItem("test02").addNamesItem("does-not-exists");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().hasSize(2);
        var w = dto.getWorkspaces().get("test01");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("test01");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(3)
                .contains(new EximWorkspaceRoleDTOV1().name("role-1-2").description("d1"));
        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(2)
                .contains(new EximProductDTOV1().productName("onecx-core").baseUrl("/core")
                        .microfrontends(List.of(new EximMicrofrontendDTOV1().appId("menu").basePath("/menu"),
                                new EximMicrofrontendDTOV1().appId("theme").basePath("/theme"))));

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(3);

        assertThat(w.getImages()).isNotNull().isNotEmpty().hasSize(2);
        assertThat(w.getMenuItems()).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    void exportWorkspaceNoMenuTest() {
        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1()
                .includeMenus(false)
                .addNamesItem("test01").addNamesItem("test02").addNamesItem("does-not-exists");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().hasSize(2);
        var w = dto.getWorkspaces().get("test01");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("test01");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(3)
                .contains(new EximWorkspaceRoleDTOV1().name("role-1-2").description("d1"));
        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(2)
                .contains(new EximProductDTOV1().productName("onecx-core").baseUrl("/core")
                        .microfrontends(List.of(new EximMicrofrontendDTOV1().appId("menu").basePath("/menu"),
                                new EximMicrofrontendDTOV1().appId("theme").basePath("/theme"))));

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(3);

        assertThat(w.getImages()).isNotNull().isNotEmpty().hasSize(2);
        assertThat(w.getMenuItems()).isNull();
    }

    @Test
    void exportAllWorkspaceTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(new ExportWorkspacesRequestDTOV1().names(new HashSet<>()))
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).hasSize(3);
    }

    @Test
    void exportWorkspaceNotFoundTest() {
        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("12345");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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

        var slots = new ArrayList<EximSlotDTOV1>();
        slots.add(new EximSlotDTOV1().name("slot1")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component1"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component2")));
        slots.add(new EximSlotDTOV1().name("slot2")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component3"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component4")));

        var menuChild11 = new EximWorkspaceMenuItemDTOV1().name("child11").key("c11").position(0);
        var menuChild1 = new EximWorkspaceMenuItemDTOV1().name("child1").key("c1").position(0).addChildrenItem(menuChild11);

        var menuChild2 = new EximWorkspaceMenuItemDTOV1().name("child2").key("c2").position(0).addRolesItem("role2");

        var menuItems = new ArrayList<EximWorkspaceMenuItemDTOV1>();
        menuItems.add(new EximWorkspaceMenuItemDTOV1().name("test1").key("p1").position(0).addRolesItem("role1")
                .addRolesItem("role-x")
                .addChildrenItem(menuChild1));
        menuItems.add(new EximWorkspaceMenuItemDTOV1().name("test2").key("p2").position(1).addRolesItem("role1")
                .addRolesItem("role-x")
                .addChildrenItem(menuChild2));

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        var products = new ArrayList<EximProductDTOV1>();
        var microFrontends = new ArrayList<EximMicrofrontendDTOV1>();
        microFrontends.add(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"));
        products.add(new EximProductDTOV1()
                .productName("product1")
                .baseUrl("/productBase")
                .microfrontends(microFrontends));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .putImagesItem("logo", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .putImagesItem("logo2", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .baseUrl("/someurl")
                .name("testWorkspace")
                .disabled(true)
                .roles(roles)
                .menuItems(menuItems)
                .products(products)
                .slots(slots);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("testWorkspace", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("testWorkspace", ImportResponseStatusDTOV1.CREATED);

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("testWorkspace");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("testWorkspace");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("testWorkspace");
        assertThat(w.getDisabled()).isEqualTo(true);

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(1)
                .containsExactly(new EximProductDTOV1().productName("product1").baseUrl("/productBase")
                        .microfrontends(List.of(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"))));

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    void importWorkspaceEmptyMenuProductTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .baseUrl("/someurl")
                .menuItems(List.of())
                .name("testWorkspace")
                .roles(roles);

        workspace.setImages(null);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("testWorkspace", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("testWorkspace", ImportResponseStatusDTOV1.CREATED);

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("testWorkspace");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("testWorkspace");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("testWorkspace");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isEmpty();
    }

    @Test
    void importWorkspaceWithoutProductTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .baseUrl("/someurl")
                .menuItems(null)
                .name("testWorkspace")
                .roles(roles);

        workspace.setImages(null);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("testWorkspace", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("testWorkspace", ImportResponseStatusDTOV1.CREATED);

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("testWorkspace");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("testWorkspace");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("testWorkspace");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isEmpty();
    }

    @Test
    void importWorkspaceAlreadyExistsTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1();
        workspace.setBaseUrl("/company01");
        workspace.setName("test01");
        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("test01", workspace);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("test01", ImportResponseStatusDTOV1.SKIPPED);
    }

    @Test
    void importWorkspacesWithOneInvalidWorkspaceTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();
        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1();
        workspace.setBaseUrl("/company01");
        workspace.setName("test01");
        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("test01", workspace);
        map.put("test", null);
        snapshot.setWorkspaces(map);

        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportWorkspaceResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getWorkspaces()).containsEntry("test01", ImportResponseStatusDTOV1.SKIPPED);
        assertThat(importResponse.getWorkspaces()).containsEntry("test", ImportResponseStatusDTOV1.ERROR);

    }

    @Test
    void importWorkspaceNoBodyTest() {
        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .get("/test01/menu/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMenu()).isNotNull();
        assertThat(dto.getMenu().getMenuItems()).isNotNull().isNotEmpty().hasSize(2);
        var c1 = dto.getMenu().getMenuItems().get(0);
        assertThat(c1).isNotNull();
        assertThat(c1.getKey()).isEqualTo("PORTAL_MAIN_MENU");
        assertThat(c1.getRoles()).isNotNull().isNotEmpty().hasSize(1).containsExactly("role-1-1");

        var c2 = dto.getMenu().getMenuItems().get(1);
        assertThat(c2).isNotNull();
        assertThat(c2.getKey()).isEqualTo("PORTAL_MAIN_MENU2");
        assertThat(c2.getRoles()).isNotNull().isNotEmpty().hasSize(1).containsExactly("role-1-2");
        assertThat(c2.getChildren()).isNotNull().isNotEmpty().hasSize(2);

        var c3 = c2.getChildren().get(0);
        assertThat(c3).isNotNull();
        assertThat(c3.getKey()).isEqualTo("PORTAL_MAIN_MENU3");
        assertThat(c3.getRoles()).isNotNull().isNotEmpty().hasSize(1).containsExactly("role-1-3");

        var c4 = c2.getChildren().get(1);
        assertThat(c4).isNotNull();
        assertThat(c4.getKey()).isEqualTo("PORTAL_MAIN_MENU4");
        assertThat(c4.getRoles()).isEmpty();
    }

    @Test
    void exportMenuByWorkspaceIdNotFoundTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .get("/test05/menu/export")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        assertThat(dto).isNotNull();
    }

    @Test
    void importMenuByWorkspaceIdEmptyListTest() {

        var snapshot = new MenuSnapshotDTOV1()
                .id("test-import-1")
                .created(OffsetDateTime.now())
                .menu(new EximMenuStructureDTOV1());

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test01/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ImportResponseStatusDTOV1.SKIPPED);

        snapshot.getMenu().menuItems(new ArrayList<>());

        response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test01/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ImportResponseStatusDTOV1.SKIPPED);
    }

    @Test
    void importMenuByWorkspaceIdTest() {

        // children of key2
        List<EximWorkspaceMenuItemDTOV1> children = new ArrayList<>();
        children.add(new EximWorkspaceMenuItemDTOV1().key("key3").name("key3").position(10).roles(Set.of("role-i-2")));
        children.add(new EximWorkspaceMenuItemDTOV1().key("key4").name("key4").position(20));

        // root menu
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(
                new EximWorkspaceMenuItemDTOV1().key("key1").name("key1").position(0).roles(Set.of("role-1-1", "role-i-2")));
        menuItems
                .add(new EximWorkspaceMenuItemDTOV1().key("key2").name("key2").position(1).roles(Set.of("role-i-3", "role-i-2"))
                        .children(children));

        var snapshot = new MenuSnapshotDTOV1()
                .id("test-import-1")
                .created(OffsetDateTime.now())
                .menu(new EximMenuStructureDTOV1().menuItems(menuItems));

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test01/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ImportResponseStatusDTOV1.UPDATED);

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .get("/test01/menu/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getMenu()).isNotNull();
        assertThat(dto.getMenu().getMenuItems()).isNotNull().isNotEmpty().hasSize(2);

        var map = dto.getMenu().getMenuItems().stream().collect(Collectors.toMap(EximWorkspaceMenuItemDTOV1::getKey, x -> x));

        var c1 = map.get("key1");
        assertThat(c1).isNotNull();
        assertThat(c1.getKey()).isEqualTo("key1");
        assertThat(c1.getRoles()).isNotNull().isNotEmpty().hasSize(2).containsExactly("role-1-1", "role-i-2");

        var c2 = map.get("key2");
        assertThat(c2).isNotNull();
        assertThat(c2.getKey()).isEqualTo("key2");
        assertThat(c2.getRoles()).isNotNull().isNotEmpty().hasSize(2).containsExactly("role-i-3", "role-i-2");
        assertThat(c2.getChildren()).isNotNull().isNotEmpty().hasSize(2);

        map = c2.getChildren().stream().collect(Collectors.toMap(EximWorkspaceMenuItemDTOV1::getKey, x -> x));

        var c3 = map.get("key3");
        assertThat(c3).isNotNull();
        assertThat(c3.getKey()).isEqualTo("key3");
        assertThat(c3.getRoles()).isNotNull().isNotEmpty().hasSize(1).containsExactly("role-i-2");

        var c4 = map.get("key4");
        assertThat(c4).isNotNull();
        assertThat(c4.getKey()).isEqualTo("key4");
        assertThat(c4.getRoles()).isEmpty();
    }

    @Test
    void importMenuByWorkspaceIdNewRolesTest() {

        // children of key2
        List<EximWorkspaceMenuItemDTOV1> children = new ArrayList<>();
        children.add(new EximWorkspaceMenuItemDTOV1().key("key3").name("key3").position(10).roles(Set.of("role-i-2")));
        children.add(new EximWorkspaceMenuItemDTOV1().key("key4").name("key4").position(20));

        // root menu
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(
                new EximWorkspaceMenuItemDTOV1().key("key1").name("key1").position(0).roles(Set.of("role-i-1", "role-i-2")));
        menuItems
                .add(new EximWorkspaceMenuItemDTOV1().key("key2").name("key2").position(1).roles(Set.of("role-i-3", "role-i-2"))
                        .children(children));

        var snapshot = new MenuSnapshotDTOV1()
                .id("test-import-1")
                .created(OffsetDateTime.now())
                .menu(new EximMenuStructureDTOV1().menuItems(menuItems));

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test02/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ImportResponseStatusDTOV1.CREATED);
    }

    @Test
    void importMenuByWorkspaceIdUpdateExistingMenuTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setPosition(0);
        menuItem.setKey("testKey");
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test01/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(ImportResponseStatusDTOV1.UPDATED);
    }

    @Test
    void importMenuByWorkspaceIdNoRolesMenuTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setPosition(0);
        menuItem.setKey("new-key-1");
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("/test03/menu/import")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ImportMenuResponseDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(ImportResponseStatusDTOV1.CREATED);
    }

    @Test
    void importMenuToNonExistingWorkspaceTest() {
        MenuSnapshotDTOV1 snapshot = new MenuSnapshotDTOV1();
        EximMenuStructureDTOV1 menu = new EximMenuStructureDTOV1();
        EximWorkspaceMenuItemDTOV1 menuItem = new EximWorkspaceMenuItemDTOV1();
        List<EximWorkspaceMenuItemDTOV1> menuItems = new ArrayList<>();
        menuItems.add(menuItem);
        menuItem.setKey("testKey");
        menuItem.setPosition(0);
        menu.setMenuItems(menuItems);
        snapshot.setMenu(menu);

        var error = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
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

    @Test
    void importOperatorThemesEmptyTest() {

        var request = new WorkspaceSnapshotDTOV1();

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("operator")
                .then()
                .statusCode(OK.getStatusCode());

        request.setWorkspaces(null);
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("operator")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void importOperatorWorkspaceNoBodyTest() {
        var importResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .post("operator")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(EximProblemDetailResponseDTOV1.class);

        assertThat(importResponse).isNotNull();
        assertThat(importResponse.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
    }

    @Test
    void importOperatorWorkspaceTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();

        var slots = new ArrayList<EximSlotDTOV1>();
        slots.add(new EximSlotDTOV1().name("slot1")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component1"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component2")));
        slots.add(new EximSlotDTOV1().name("slot2")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component3"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component4")));

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        var products = new ArrayList<EximProductDTOV1>();
        var microFrontends = new ArrayList<EximMicrofrontendDTOV1>();
        microFrontends.add(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"));
        products.add(new EximProductDTOV1()
                .productName("product1")
                .baseUrl("/productBase")
                .microfrontends(microFrontends));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .putImagesItem("logo", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .putImagesItem("logo2", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .baseUrl("/someurl")
                .name("testWorkspace")
                .roles(roles)
                .menuItems(null)
                .products(products)
                .slots(slots);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("test01", workspace);
        snapshot.setWorkspaces(map);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("operator")
                .then()
                .statusCode(OK.getStatusCode());

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("test01");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("test01");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("test01");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(1)
                .containsExactly(new EximProductDTOV1().productName("product1").baseUrl("/productBase")
                        .microfrontends(List.of(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"))));

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    void importOperatorNewWorkspaceTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();

        var slots = new ArrayList<EximSlotDTOV1>();
        slots.add(new EximSlotDTOV1().name("slot1")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component1"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app1")
                                .productName("product1")
                                .name("component2")));
        slots.add(new EximSlotDTOV1().name("slot2")
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component3"))
                .addComponentsItem(
                        new EximComponentDTOV1()
                                .appId("app2")
                                .productName("product2")
                                .name("component4")));

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        var products = new ArrayList<EximProductDTOV1>();
        var microFrontends = new ArrayList<EximMicrofrontendDTOV1>();
        microFrontends.add(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"));
        products.add(new EximProductDTOV1()
                .productName("product1")
                .baseUrl("/productBase")
                .microfrontends(microFrontends));

        var menuChild11 = new EximWorkspaceMenuItemDTOV1().name("child11").key("c11").position(0);
        var menuChild1 = new EximWorkspaceMenuItemDTOV1().name("child1").key("c1").position(0).addChildrenItem(menuChild11);

        var menuChild2 = new EximWorkspaceMenuItemDTOV1().name("child2").key("c2").position(0).addRolesItem("role2");

        var menuItems = new ArrayList<EximWorkspaceMenuItemDTOV1>();
        menuItems.add(new EximWorkspaceMenuItemDTOV1().name("test1").key("p1").position(0).addRolesItem("role1")
                .addRolesItem("role-x")
                .addChildrenItem(menuChild1));
        menuItems.add(new EximWorkspaceMenuItemDTOV1().name("test2").key("p2").position(1).addRolesItem("role1")
                .addRolesItem("role-x")
                .addChildrenItem(menuChild2));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .putImagesItem("logo", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .putImagesItem("logo2", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .baseUrl("/someurl")
                .name("testWorkspace")
                .roles(roles)
                .products(products)
                .menuItems(menuItems)
                .slots(slots);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("new_test_workspace", workspace);
        snapshot.setWorkspaces(map);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("operator")
                .then()
                .statusCode(OK.getStatusCode());

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("new_test_workspace");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("new_test_workspace");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("new_test_workspace");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isNotNull().isNotEmpty().hasSize(1)
                .containsExactly(new EximProductDTOV1().productName("product1").baseUrl("/productBase")
                        .microfrontends(List.of(new EximMicrofrontendDTOV1().appId("app1").basePath("/app1"))));

        assertThat(w.getSlots()).isNotNull().isNotEmpty().hasSize(2);

        assertThat(w.getMenuItems()).isNotNull().isNotEmpty().hasSize(2);
    }

    @Test
    void importOperatorNoSlotsNoProductsWorkspaceTest() {
        WorkspaceSnapshotDTOV1 snapshot = new WorkspaceSnapshotDTOV1();

        var roles = new ArrayList<EximWorkspaceRoleDTOV1>();
        roles.add(new EximWorkspaceRoleDTOV1().name("role1").description("role1"));
        roles.add(new EximWorkspaceRoleDTOV1().name("role2").description("role2"));

        EximWorkspaceDTOV1 workspace = new EximWorkspaceDTOV1()
                .putImagesItem("logo", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .putImagesItem("logo2", new ImageDTOV1().imageData(new byte[] { 1, 2, 3 }).mimeType("image/*"))
                .baseUrl("/someurl")
                .name("testWorkspace")
                .roles(roles);

        Map<String, EximWorkspaceDTOV1> map = new HashMap<>();
        map.put("new_test_workspace", workspace);
        snapshot.setWorkspaces(map);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(snapshot)
                .post("operator")
                .then()
                .statusCode(OK.getStatusCode());

        ExportWorkspacesRequestDTOV1 request = new ExportWorkspacesRequestDTOV1();
        request.addNamesItem("new_test_workspace");
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .post("/export")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceSnapshotDTOV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaces()).isNotNull().isNotEmpty();
        var w = dto.getWorkspaces().get("new_test_workspace");
        assertThat(w).isNotNull();
        assertThat(w.getName()).isEqualTo("new_test_workspace");

        assertThat(w.getRoles()).isNotNull().isNotEmpty().hasSize(2)
                .containsExactly(
                        new EximWorkspaceRoleDTOV1().name("role1").description("role1"),
                        new EximWorkspaceRoleDTOV1().name("role2").description("role2"));
        assertThat(w.getProducts()).isNotNull().isEmpty();
        assertThat(w.getSlots()).isNotNull().isEmpty();
    }
}
