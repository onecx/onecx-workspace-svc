package org.tkit.onecx.workspace.rs.admin.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.admin.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(WorkspaceAdminRestControllerV1.class)
@WithDBData(value = "data/testdata-admin.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:admin-read", "ocx-ws:admin-write",
        "ocx-ws:admin-delete" })
class WorkspaceAdminRestControllerV1Test extends AbstractTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createWorkspaceTest() {

        // create workspace
        var createWorkspaceDTOAdminV1 = new CreateWorkspaceRequestDTOAdminV1();
        createWorkspaceDTOAdminV1
                .name("Workspace1")
                .displayName("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTOAdminV1)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTOAdminV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo(createWorkspaceDTOAdminV1.getName());
        assertThat(dto.getDisplayName()).isNotNull().isEqualTo(createWorkspaceDTOAdminV1.getDisplayName());
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTOAdminV1.getCompanyName());
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTOAdminV1.getBaseUrl());

        // create without body
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);

        assertThat(exception.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(exception.getDetail()).isEqualTo("createWorkspace.createWorkspaceRequestDTOAdminV1: must not be null");

        exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTOAdminV1)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'workspace_base_url_tenant_id'  Detail: Key (base_url, tenant_id)=(/work1, tenant-100) already exists.]");

        createWorkspaceDTOAdminV1.setName("custom-new-name");

        exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTOAdminV1)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'workspace_base_url_tenant_id'  Detail: Key (base_url, tenant_id)=(/work1, tenant-100) already exists.]");
    }

    @Test
    void deleteWorkspace() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getWorkspace() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOAdminV1.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo("test01");
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo("Company1");
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo("/company1");
        assertThat(dto.getAddress()).isNotNull();
        assertThat(dto.getAddress().getStreetNo()).isEqualTo("6");
    }

    @Test
    void searchWorkspacesTest() {
        var criteria = new WorkspaceSearchCriteriaDTOAdminV1();

        // empty criteria
        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTOAdminV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

        criteria.setName("test01");
        criteria.setThemeName("11-111");

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTOAdminV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);

        criteria.setName("");
        criteria.setThemeName("   ");

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTOAdminV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

        criteria.setName(" _ ");

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTOAdminV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isZero();
        assertThat(data.getStream()).isNotNull().isEmpty();
    }

    @Test
    void updateWorkspaceTest() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var response = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOAdminV1.class);
        response.setDisplayName("11-222");

        UpdateWorkspaceRequestDTOAdminV1 requestDTOAdminV1 = new UpdateWorkspaceRequestDTOAdminV1();
        requestDTOAdminV1.setResource(objectMapper.convertValue(response, UpdateWorkspaceDTOAdminV1.class));

        // update none existing workspace
        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOAdminV1)
                .pathParam("id", "none-exists")
                .put("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update workspace with already existing baseUrl for other workspace
        response.setBaseUrl("/company1");
        requestDTOAdminV1.setResource(objectMapper.convertValue(response, UpdateWorkspaceDTOAdminV1.class));

        var error = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOAdminV1)
                .pathParam("id", "11-222")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("MERGE_ENTITY_FAILED");
        assertThat(error.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'workspace_base_url_tenant_id'  Detail: Key (base_url, tenant_id)=(/company1, tenant-100) already exists.]");

        // normal update
        response.setBaseUrl("/company2/updated");
        response.setCompanyName("Company 2 updated");
        response.setName("Workspace2Test");
        response.setDisplayName("Workspace2TestDisplay");
        requestDTOAdminV1.setResource(objectMapper.convertValue(response, UpdateWorkspaceDTOAdminV1.class));

        var updatedWorkspace = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOAdminV1)
                .pathParam("id", "11-222")
                .put("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(WorkspaceDTOAdminV1.class);

        assertThat(updatedWorkspace).isNotNull();
        assertThat(updatedWorkspace.getName()).isEqualTo(response.getName());

        var updatedResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOAdminV1.class);

        assertThat(updatedResponse).isNotNull();
        assertThat(updatedResponse.getAddress()).isNotNull();
        assertThat(updatedResponse.getAddress().getStreetNo()).isEqualTo(response.getAddress().getStreetNo());
        assertThat(updatedResponse.getAddress().getStreet()).isEqualTo(response.getAddress().getStreet());
        assertThat(updatedResponse.getCompanyName()).isEqualTo(response.getCompanyName());
        assertThat(updatedResponse.getBaseUrl()).isEqualTo(response.getBaseUrl());

        // update second time
        updatedResponse.setBaseUrl("/company2/test");
        updatedResponse.setCompanyName("Company 2 test");
        updatedResponse.setName("Workspace2Test");
        updatedResponse.setDisplayName("Workspace2Test");
        updatedResponse.setModificationCount(0);
        requestDTOAdminV1.setResource(objectMapper.convertValue(updatedResponse, UpdateWorkspaceDTOAdminV1.class));
        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOAdminV1)
                .pathParam("id", "11-222")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);
    }

    @Test
    void modificationCountTest() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var updatedResponse = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTOAdminV1.class);
        UpdateWorkspaceRequestDTOAdminV1 requestDTOAdminV1 = new UpdateWorkspaceRequestDTOAdminV1();
        requestDTOAdminV1.setResource(objectMapper.convertValue(updatedResponse, UpdateWorkspaceDTOAdminV1.class));
        // update second time
        updatedResponse.setBaseUrl("/company2/test");
        updatedResponse.setCompanyName("Company 2 test");
        updatedResponse.setName("Workspace2Test");
        updatedResponse.setModificationCount(0);
        given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(requestDTOAdminV1)
                .pathParam("id", "11-222")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTOAdminV1.class);
    }

}
