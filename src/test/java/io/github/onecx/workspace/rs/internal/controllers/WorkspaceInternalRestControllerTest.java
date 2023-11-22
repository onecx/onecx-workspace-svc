package io.github.onecx.workspace.rs.internal.controllers;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import gen.io.github.onecx.workspace.rs.internal.model.*;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class WorkspaceInternalRestControllerTest extends AbstractTest {

    @Test
    void createWorkspaceTest() {

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .workspaceName("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post()
                .then().log().all()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaceName()).isNotNull().isEqualTo(createWorkspaceDTO.getWorkspaceName());
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        // create without body
        var exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("createWorkspace.createWorkspaceRequestDTO: must not be null");

        exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .post()
                .then().log().all()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'workspace_base_url_key'  Detail: Key (base_url)=(/work1) already exists.]");
    }

    @Test
    void deleteWorkspace() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "22-111")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        given().contentType(APPLICATION_JSON)
                .pathParam("id", "22-111")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "22-111")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getWorkspace() {
        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get("{id}")
                .then().log().all()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getWorkspaceName()).isNotNull().isEqualTo("test01");
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo("Company1");
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo("/company1");
        assertThat(dto.getAddress()).isNotNull();
        assertThat(dto.getAddress().getStreetNo()).isEqualTo("6");
        assertThat(dto.getImageUrls()).isNotEmpty();
        assertThat(dto.getSubjectLinks()).isNotEmpty();
    }

    @Test
    void searchWorkspacesTest() {
        var criteria = new WorkspaceSearchCriteriaDTO();

        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setWorkspaceName("test01");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);

        criteria.setWorkspaceName(" ");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);
    }

    @Test
    void updateWorkspaceTest() {

    }
}
