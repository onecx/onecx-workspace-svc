package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.LOCATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.rs.internal.mappers.InternalExceptionMapper;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AssignmentInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class AssignmentRestControllerTest extends AbstractTest {

    @Test
    void createAssignment() {
        // create Assignment
        var requestDTO = new CreateAssignmentRequestDTO();
        requestDTO.setMenuItemId("33-2");
        requestDTO.setRoleId("r11");

        var uri = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().header(LOCATION);

        var dto = given()
                .contentType(APPLICATION_JSON)
                .get(uri)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .body().as(AssignmentDTO.class);

        assertThat(dto).isNotNull()
                .returns(requestDTO.getRoleId(), from(AssignmentDTO::getRoleId))
                .returns(requestDTO.getMenuItemId(), from(AssignmentDTO::getMenuItemId));

        // create Role without body
        var exception = given()
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(InternalExceptionMapper.TechnicalErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("createAssignment.createAssignmentRequestDTO: must not be null");

        // create Role with existing name
        requestDTO = new CreateAssignmentRequestDTO();
        requestDTO.setMenuItemId("33-6");
        requestDTO.setRoleId("r13");

        exception = given().when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("PERSIST_ENTITY_FAILED");
        assertThat(exception.getDetail()).isEqualTo(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'assignment_key'  Detail: Key (tenant_id, role_id, menu_item_id)=(tenant-100, r13, 33-6) already exists.]");

    }

    @Test
    void createAssignmentWrong() {
        // create Assignment
        var requestDTO = new CreateAssignmentRequestDTO()
                .menuItemId("does-not-exists")
                .roleId("r11");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        requestDTO.menuItemId("p11");
        requestDTO.setRoleId("does-not-exists");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getNotFoundAssignment() {
        given()
                .contentType(APPLICATION_JSON)
                .get("does-not-exists")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void searchAssignmentTest() {
        var criteria = new AssignmentSearchCriteriaDTO();

        criteria.setMenuItemId("    ");
        criteria.setWorkspaceId("    ");
        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setWorkspaceId("11-111");
        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setMenuItemId(null);
        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setMenuItemId("does-not-exists");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(0);
        assertThat(data.getStream()).isNotNull().hasSize(0);

        var criteria2 = new AssignmentSearchCriteriaDTO();

        criteria2.setMenuItemId("33-6");

        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria2)
                .post("/search")
                .then().log().all()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);

        //get by multiple appIds
        var multipleAppIdsCriteria = new AssignmentSearchCriteriaDTO();
        multipleAppIdsCriteria.setMenuItemId("55-2");

        var multipleAppIdsResult = given()
                .contentType(APPLICATION_JSON)
                .body(multipleAppIdsCriteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AssignmentPageResultDTO.class);

        assertThat(multipleAppIdsResult).isNotNull();
        assertThat(multipleAppIdsResult.getTotalElements()).isEqualTo(0);
        assertThat(multipleAppIdsResult.getStream()).isNotNull().hasSize(0);

    }

    @Test
    void deleteAssignmentTest() {

        // delete Assignment
        given()
                .contentType(APPLICATION_JSON)
                .delete("DELETE_1")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // check Assignment
        given()
                .contentType(APPLICATION_JSON)
                .get("a11")
                .then()
                .statusCode(OK.getStatusCode());

        // check if Assignment does not exist
        given()
                .contentType(APPLICATION_JSON)
                .delete("a11")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        // check Assignment
        given()
                .contentType(APPLICATION_JSON)
                .get("a11")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

    }
}
