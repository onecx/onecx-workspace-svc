package org.tkit.onecx.workspace.rs.user.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuRequestDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuStructureDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserMenuInternalController.class)
@WithDBData(value = "data/testdata-user.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:all", "ocx-ws:read", "ocx-ws:write" })
class UserMenuInternalControllerInterpolateTest extends AbstractTest {

    @Test
    void getMenuStructure_shouldReturnSystemURL() {

        var workspaceName = "test03";
        var accessToken = createAccessTokenBearer(USER_BOB);
        var idToken = createToken("org1");

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then().log().all()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenu().get(1).getChildren().get(1).getUrl()).isEqualTo("/company3/testItem1");
    }

}
