package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.workspace.domain.template.models.CreateTemplateConfig;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:all", "ocx-ws:read", "ocx-ws:write", "ocx-ws:delete" })
class WorkspaceInternalCustomConfigRestControllerTest extends AbstractTest {

    @InjectMock
    CreateTemplateConfig templateConfig;

    @Inject
    Config config;

    @Test
    @SuppressWarnings("java:S5976")
    void createWorkspaceMissingResourceTest() {

        var m = Mockito.mock(CreateTemplateConfig.Create.class);
        Mockito.when(templateConfig.create()).thenReturn(m);
        Mockito.when(m.resource()).thenReturn("template/missing-file.json");
        Mockito.when(m.classPathResource()).thenReturn(true);
        Mockito.when(m.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace-missing-resource")
                .displayName("Workspace-missing-resource")
                .companyName("Company-missing-resource")
                .baseUrl("/work-missing-resource");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    @SuppressWarnings("java:S5976")
    void createWorkspaceWrongFileTest() {

        var m = Mockito.mock(CreateTemplateConfig.Create.class);
        Mockito.when(templateConfig.create()).thenReturn(m);
        Mockito.when(m.resource()).thenReturn("./src/test/resources/template/missing-file.json");
        Mockito.when(m.classPathResource()).thenReturn(false);
        Mockito.when(m.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace-wrong-file")
                .displayName("Workspace-wrong-file")
                .companyName("Company1")
                .baseUrl("/work1");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    @SuppressWarnings("java:S5961")
    void createWorkspaceDisabledTest() {

        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(CreateTemplateConfig.class);

        var m = Mockito.mock(CreateTemplateConfig.Create.class);
        Mockito.when(templateConfig.create()).thenReturn(m);

        Mockito.when(m.resource()).thenReturn(tmp.create().resource());
        Mockito.when(m.classPathResource()).thenReturn(tmp.create().classPathResource());
        Mockito.when(m.enabled()).thenReturn(false);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .displayName("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var responseDto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(responseDto.getDisplayName()).isNotNull().isEqualTo(createWorkspaceDTO.getDisplayName());
        assertThat(responseDto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(responseDto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", responseDto.getId())
                .get("/internal/workspaces/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo("Workspace1");
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo("Company1");
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo("/work1");

        var criteria = new RoleSearchCriteriaDTO();
        criteria.workspaceId(responseDto.getId());

        var rolesResult = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/internal/roles/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(RolePageResultDTO.class);

        assertThat(rolesResult).isNotNull();
        assertThat(rolesResult.getTotalElements()).isZero();
        assertThat(rolesResult.getStream()).isNotNull().isEmpty();

        var slotsResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", responseDto.getId())
                .get("/internal/slots/workspace/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspaceSlotsDTO.class);

        assertThat(slotsResponse).isNotNull();
        assertThat(slotsResponse.getSlots()).isNotNull().isEmpty();

        var productsResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(new ProductSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/products/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(productsResponse).isNotNull();
        assertThat(productsResponse.getStream()).isNotNull().isEmpty();

        var menuResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(new MenuStructureSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/menuItems/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemStructureDTO.class);

        assertThat(menuResponse).isNotNull();
        assertThat(menuResponse.getMenuItems()).isNotNull().isEmpty();

        var assignmentResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(new AssignmentSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/assignments/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(assignmentResponse).isNotNull();
        assertThat(assignmentResponse.getStream()).isNotNull().isEmpty();
    }

    @Test
    @SuppressWarnings("java:S5976")
    void createWorkspaceWrongJsonFormatTest() {

        var m = Mockito.mock(CreateTemplateConfig.Create.class);
        Mockito.when(templateConfig.create()).thenReturn(m);

        Mockito.when(m.resource())
                .thenReturn("./src/test/resources/template/not-valid-workspace-create-test.json");
        Mockito.when(m.classPathResource()).thenReturn(false);
        Mockito.when(m.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .displayName("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @SuppressWarnings("java:S5961")
    void createWorkspaceFileTest() {
        var m = Mockito.mock(CreateTemplateConfig.Create.class);
        Mockito.when(templateConfig.create()).thenReturn(m);

        Mockito.when(m.resource()).thenReturn("./src/test/resources/template/workspace-create-test.json");
        Mockito.when(m.classPathResource()).thenReturn(false);
        Mockito.when(m.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .displayName("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var responseDto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(responseDto.getDisplayName()).isNotNull().isEqualTo(createWorkspaceDTO.getDisplayName());
        assertThat(responseDto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(responseDto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", responseDto.getId())
                .get("/internal/workspaces/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo("Workspace1");
        assertThat(dto.getDisplayName()).isNotNull().isEqualTo("Workspace1");
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo("Company1");
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo("/work1");

        var criteria = new RoleSearchCriteriaDTO();
        criteria.workspaceId(responseDto.getId());

        var rolesResult = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/internal/roles/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(RolePageResultDTO.class);

        assertThat(rolesResult).isNotNull();
        assertThat(rolesResult.getTotalElements()).isEqualTo(2);
        assertThat(rolesResult.getStream()).isNotNull().hasSize(2);

        var names = rolesResult.getStream().stream().map(RoleDTO::getName).toList();
        assertThat(names).containsOnly("onecx-admin", "onecx-user");

        var slotsResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", responseDto.getId())
                .get("/internal/slots/workspace/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspaceSlotsDTO.class);

        assertThat(slotsResponse).isNotNull();
        assertThat(slotsResponse.getSlots()).isNotNull().hasSize(3);
        assertThat(slotsResponse.getSlots().get(0)).isNotNull();
        var slotMap = slotsResponse.getSlots().stream().collect(Collectors.toMap(SlotDTO::getName, x -> x));
        assertThat(slotMap).containsOnlyKeys("onecx-shell-vertical-menu", "onecx-shell-header-right",
                "onecx-shell-horizontal-menu");
        assertThat(slotMap.get("onecx-shell-vertical-menu").getComponents()).isNotNull().hasSize(2);

        var productsResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(new ProductSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/products/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(ProductPageResultDTO.class);

        assertThat(productsResponse).isNotNull();
        assertThat(productsResponse.getStream()).isNotNull().isNotEmpty().hasSize(4);

        var productName = productsResponse.getStream().stream().map(ProductResultDTO::getProductName).toList();
        assertThat(productName).containsOnly("onecx-user-profile", "onecx-workspace", "onecx-shell", "onecx-welcome");

        var menuResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(new MenuStructureSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/menuItems/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemStructureDTO.class);

        assertThat(menuResponse).isNotNull();
        List<WorkspaceMenuItemDTO> allItems = new ArrayList<>();
        for (WorkspaceMenuItemDTO item : menuResponse.getMenuItems()) {
            allItems.add(item);
            if (item.getChildren() != null) {
                allItems.addAll(item.getChildren());
            }
        }

        assertThat(allItems).isNotNull().isNotEmpty().hasSize(9);
        var parents = menuResponse.getMenuItems().stream().filter(x -> x.getParentItemId() == null).toList();
        assertThat(parents).isNotNull().isNotEmpty().hasSize(3);
        var parentNames = parents.stream().map(WorkspaceMenuItemDTO::getName).toList();
        assertThat(parentNames).containsOnly("Footer Menu", "User Profile Menu", "Main Menu");

        var assignmentResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(new AssignmentSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/assignments/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(assignmentResponse).isNotNull();
        assertThat(assignmentResponse.getStream()).isNotNull().isNotEmpty().hasSize(7);
    }

}
