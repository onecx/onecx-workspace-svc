package org.tkit.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.tkit.onecx.workspace.rs.legacy.controllers.PortalLegacyRestControllerExceptionTest.ErrorKey.ERROR_TEST;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.workspace.domain.daos.WorkspaceDAO;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import gen.org.tkit.onecx.workspace.rs.legacy.model.RestExceptionDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TkitPortalRestController.class)
class TkitPortalRestControllerExceptionTest extends AbstractTest {

    @InjectMock
    WorkspaceDAO dao;

    @BeforeEach
    void beforeAll() {
        Mockito.when(dao.findByWorkspaceName(notNull()))
                .thenThrow(new RuntimeException("Test technical error exception"))
                .thenThrow(new DAOException(ERROR_TEST, new RuntimeException("Test")));
    }

    @Test
    void getMenuStructureForNoPortalNameTest() {
        var exception = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "TEST_ERROR")
                .get()
                .then().statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .extract().as(RestExceptionDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo("UNDEFINED_ERROR_CODE");

        exception = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "TEST_ERROR")
                .get()
                .then().statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(RestExceptionDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(ERROR_TEST.name());
    }

}
