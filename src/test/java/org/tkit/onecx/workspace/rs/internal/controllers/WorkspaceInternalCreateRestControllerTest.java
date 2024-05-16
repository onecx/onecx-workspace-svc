package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)

class WorkspaceInternalCreateRestControllerTest extends AbstractTest {

    @Test
    @SuppressWarnings("java:S5961")
    void createWorkspaceTest() {

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO()
                .name("Workspace-Create")
                .companyName("Company-Create")
                .baseUrl("/work-create");

        var responseDto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post("/internal/workspaces")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(responseDto).isNotNull();

        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", responseDto.getId())
                .get("/internal/workspaces/{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

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
        assertThat(rolesResult.getStream()).isNotNull().hasSize(2);

        var names = rolesResult.getStream().stream().map(RoleDTO::getName).toList();
        assertThat(names).containsOnly("onecx-admin", "onecx-user-test");

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
        assertThat(slotMap).containsOnlyKeys("menu", "headerRight", "horizontalMenu");

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
