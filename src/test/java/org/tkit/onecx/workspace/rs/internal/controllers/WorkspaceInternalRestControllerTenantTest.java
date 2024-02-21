package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

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
@TestHTTPEndpoint(WorkspaceInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class WorkspaceInternalRestControllerTenantTest extends AbstractTest {

    @Test
    void createWorkspaceTest() {

        // create workspace
        var createWorkspaceDTO = new CreateWorkspaceRequestDTO();
        createWorkspaceDTO
                .name("Workspace1")
                .companyName("Company1")
                .baseUrl("/work1");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(createWorkspaceDTO)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", dto.getId())
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var workspaceDTO = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", dto.getId())
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);
        assertThat(workspaceDTO).isNotNull();
        assertThat(workspaceDTO.getName()).isNotNull().isEqualTo(createWorkspaceDTO.getName());
        assertThat(workspaceDTO.getCompanyName()).isNotNull().isEqualTo(createWorkspaceDTO.getCompanyName());
        assertThat(workspaceDTO.getBaseUrl()).isNotNull().isEqualTo(createWorkspaceDTO.getBaseUrl());

        // create without body
        var exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
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
                .header(APM_HEADER_PARAM, createToken("org1"))
                .post()
                .then()
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
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        given().contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then().statusCode(OK.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        given().contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getWorkspace() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-111")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isNotNull().isEqualTo("test01");
        assertThat(dto.getCompanyName()).isNotNull().isEqualTo("Company1");
        assertThat(dto.getBaseUrl()).isNotNull().isEqualTo("/company1");
        assertThat(dto.getAddress()).isNotNull();
        assertThat(dto.getAddress().getStreetNo()).isEqualTo("6");
        assertThat(dto.getImageUrls()).isNotEmpty();
        assertThat(dto.getSubjectLinks()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("orgAndResults")
    void searchByTenant(String organization, int results, int criteriaResults) {
        var criteria = new WorkspaceSearchCriteriaDTO();

        // empty criteria
        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .header(APM_HEADER_PARAM, createToken(organization))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(results);
        assertThat(data.getStream()).isNotNull().hasSize(results);

        criteria.setName("test01");
        criteria.setThemeName("11-111");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .header(APM_HEADER_PARAM, createToken(organization))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(criteriaResults);
        assertThat(data.getStream()).isNotNull().hasSize(criteriaResults);

        criteria.setName("");
        criteria.setThemeName("  ");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .header(APM_HEADER_PARAM, createToken(organization))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(results);
        assertThat(data.getStream()).isNotNull().hasSize(results);

        criteria.setName(" _ ");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .header(APM_HEADER_PARAM, createToken(organization))
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(WorkspacePageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isZero();
        assertThat(data.getStream()).isNotNull().isEmpty();
    }

    private static Stream<Arguments> orgAndResults() {
        return Stream.of(
                arguments("org1", 2, 1),
                arguments("org2", 1, 0),
                arguments("org3", 0, 0));
    }

    @Test
    void updateWorkspaceTest() {
        var response = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);
        // update workspace with different tenant
        given().when()
                .contentType(APPLICATION_JSON)
                .body(response)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org2"))
                .put("{id}")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        // update workspace with already existing baseUrl for other workspace
        response.setBaseUrl("/company3");
        var error = given().when()
                .contentType(APPLICATION_JSON)
                .body(response)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo("MERGE_ENTITY_FAILED");
        assertThat(error.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'workspace_base_url_key'  Detail: Key (base_url)=(/company3) already exists.]");

        // normal update
        response.setBaseUrl("/company2/updated");
        response.setCompanyName("Company 2 updated");
        response.setName("Workspace2Test");
        given().when()
                .contentType(APPLICATION_JSON)
                .body(response)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .put("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        var updatedResponse = given().when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .extract().as(WorkspaceDTO.class);

        assertThat(updatedResponse).isNotNull();
        assertThat(updatedResponse.getAddress()).isNotNull();
        assertThat(updatedResponse.getAddress().getStreetNo()).isEqualTo(response.getAddress().getStreetNo());
        assertThat(updatedResponse.getAddress().getStreet()).isEqualTo(response.getAddress().getStreet());
        assertThat(updatedResponse.getCompanyName()).isEqualTo(response.getCompanyName());
        assertThat(updatedResponse.getBaseUrl()).isEqualTo(response.getBaseUrl());
    }

}
