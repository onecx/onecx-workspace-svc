package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.workspace.domain.template.models.CreateTemplateConfig;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceInternalCustomConfigRestControllerTest extends AbstractTest {

    @InjectMock
    CreateTemplateConfig templateConfig;

    @Inject
    Config config;

    //    @Inject
    //    Config config;
    //
    //    @BeforeEach
    //    void beforeEach() {
    //        Mockito.when(templateConfig.resource()).thenReturn("./src/test/resources/template/not-valid-workspace-create.json");
    //        Mockito.when(templateConfig.classPathResource()).thenReturn(false);
    //        Mockito.when(templateConfig.enabled()).thenReturn(true);
    //    }

    @Test
    void createWorkspaceMissingResourceTest() {

        Mockito.when(templateConfig.resource()).thenReturn("template/missing-file.json");
        Mockito.when(templateConfig.classPathResource()).thenReturn(true);
        Mockito.when(templateConfig.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    void createWorkspaceWrongFileTest() {

        Mockito.when(templateConfig.resource()).thenReturn("./src/test/resources/template/missing-file.json");
        Mockito.when(templateConfig.classPathResource()).thenReturn(false);
        Mockito.when(templateConfig.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    void createWorkspaceDisabledTest() {

        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(CreateTemplateConfig.class);
        Mockito.when(templateConfig.resource()).thenReturn(tmp.resource());
        Mockito.when(templateConfig.classPathResource()).thenReturn(tmp.classPathResource());
        Mockito.when(templateConfig.enabled()).thenReturn(false);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var responseDto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(responseDto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(responseDto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        var dto = given()
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
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/internal/roles/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(RolePageResultDTO.class);

        assertThat(rolesResult).isNotNull();
        assertThat(rolesResult.getTotalElements()).isEqualTo(0);
        assertThat(rolesResult.getStream()).isNotNull().isEmpty();

        var slotsResponse = given()
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
                .when()
                .contentType(APPLICATION_JSON)
                .body(new MenuItemSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/menuItems/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(menuResponse).isNotNull();
        assertThat(menuResponse.getStream()).isNotNull().isEmpty();

        var assignmentResponse = given()
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
    void createWorkspaceWrongJsonFormatTest() {

        Mockito.when(templateConfig.resource()).thenReturn("./src/test/resources/template/not-valid-workspace-create.json");
        Mockito.when(templateConfig.classPathResource()).thenReturn(false);
        Mockito.when(templateConfig.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void createWorkspaceClassPathTest() {

        Mockito.when(templateConfig.resource()).thenReturn("template/workspace-create.json");
        Mockito.when(templateConfig.classPathResource()).thenReturn(true);
        Mockito.when(templateConfig.enabled()).thenReturn(true);

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var responseDto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(responseDto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(responseDto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        var dto = given()
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
        assertThat(slotMap.keySet()).containsOnly("menu", "headerRight", "horizontalMenu");
        assertThat(slotMap.get("menu").getComponents()).isNotNull().hasSize(2);

        var productsResponse = given()
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
                .when()
                .contentType(APPLICATION_JSON)
                .body(new MenuItemSearchCriteriaDTO().workspaceId(responseDto.getId()))
                .post("/internal/menuItems/search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(menuResponse).isNotNull();
        assertThat(menuResponse.getStream()).isNotNull().isNotEmpty().hasSize(9);
        var parents = menuResponse.getStream().stream().filter(x -> x.getParentItemId() == null).toList();
        assertThat(parents).isNotNull().isNotEmpty().hasSize(3);
        var parentNames = parents.stream().map(MenuItemResultDTO::getName).toList();
        assertThat(parentNames).containsOnly("Footer Menu", "User Profile Menu", "Main Menu");

        var assignmentResponse = given()
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
