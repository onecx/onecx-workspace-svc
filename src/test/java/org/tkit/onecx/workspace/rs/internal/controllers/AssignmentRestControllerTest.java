package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AssignmentInternalRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
public class AssignmentRestControllerTest extends AbstractTest {

    @Test
    void getNotFoundAssignment() {
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "11-222")
                .get("does-not-exists")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }
}
