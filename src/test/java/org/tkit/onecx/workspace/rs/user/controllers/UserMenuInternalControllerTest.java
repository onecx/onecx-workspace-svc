package org.tkit.onecx.workspace.rs.user.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.rs.user.mappers.ExceptionMapper;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuItemDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuRequestDTO;
import gen.org.tkit.onecx.workspace.rs.user.model.UserWorkspaceMenuStructureDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(UserMenuInternalController.class)
@WithDBData(value = "data/testdata-user.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class UserMenuInternalControllerTest extends AbstractTest {

    @Test
    void getApplicationPermissionsWrongTongTest() {

        var workspaceName = "test03";
        given()
                .contentType(APPLICATION_JSON)
                .body(new UserWorkspaceMenuRequestDTO().token("this-is-not-token"))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    void getMenuStructureNoTokenTest() {

        var workspaceName = "test03";
        var idToken = createToken("org1");

        var error = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO())
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(error).isNotNull();
        assertThat(error.getErrorCode()).isEqualTo(ExceptionMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(error.getDetail()).isEqualTo(
                "getUserMenu.userWorkspaceMenuRequestDTO.token: must not be null");
    }

    @Test
    void getMenuStructureWrongWorkspaceTest() {

        var workspaceName = "does-not-exists";
        var accessToken = createAccessTokenBearer(USER_BOB);
        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void getMenuStructureNoAssignmentTest() {

        var workspaceName = "test05";
        var accessToken = createAccessTokenBearer(USER_BOB);
        var idToken = createToken("org1");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isEmpty();
    }

    @Test
    void getMenuStructureForUserIdTest() {

        var workspaceName = "test03";
        var accessToken = createAccessTokenBearer(USER_BOB);
        var idToken = createToken("org1");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isNotEmpty();

        var output = print(data.getMenu(), "");
        System.out.println(output);

        String tmp = """
                + [0] 4-1
                  + [0] 4-1-1
                  + [1] 4-1-2
                  + [2] 4-1-3
                + [1] 4-2
                  + [0] 4-2-1
                  + [1] 4-2-2
                  + [2] 4-2-3
                    + [0] 4-2-3-1
                """;
        assertThat(output).isEqualTo(tmp);
        assertThat(countMenuItems(data.getMenu())).isEqualTo(9);
        assertThat(data.getMenu().get(0).getUrl()).contains("/company3");

        // without bearer prefix
        accessToken = createAccessToken(USER_BOB);
        data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isNotEmpty();

        output = print(data.getMenu(), "");
        System.out.println(output);
        assertThat(output).isEqualTo(tmp);
        assertThat(countMenuItems(data.getMenu())).isEqualTo(9);
    }

    @Test
    void getMenuStructureForUserIdByMenuKeyTest() {

        var workspaceName = "test03";
        var accessToken = createAccessTokenBearer(USER_BOB);
        var idToken = createToken("org1");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken).menuKeys(List.of("main-menu", "not-existing")))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isNotEmpty();

        var output = print(data.getMenu(), "");
        System.out.println(output);

        String tmp = """
                + [1] 4-2
                  + [0] 4-2-1
                  + [1] 4-2-2
                  + [2] 4-2-3
                    + [0] 4-2-3-1
                """;
        assertThat(output).isEqualTo(tmp);
        assertThat(countMenuItems(data.getMenu())).isEqualTo(5);

        // without bearer prefix
        accessToken = createAccessToken(USER_BOB);
        data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken).menuKeys(List.of("main-menu", "not-existing")))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isNotEmpty();

        output = print(data.getMenu(), "");
        System.out.println(output);
        assertThat(output).isEqualTo(tmp);
        assertThat(countMenuItems(data.getMenu())).isEqualTo(5);
    }

    @Test
    void getMenuStructureForUserIdOrg2Test() {

        var workspaceName = "test04";
        var accessToken = createAccessTokenBearer(USER_BOB);
        var idToken = createToken("org2");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, idToken)
                .body(new UserWorkspaceMenuRequestDTO().token(accessToken))
                .pathParam("workspaceName", workspaceName)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(UserWorkspaceMenuStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getWorkspaceName()).isNotNull().isEqualTo(workspaceName);
        assertThat(data.getMenu()).isNotNull().isNotEmpty();

        var output = print(data.getMenu(), "");
        System.out.println(output);

        String tmp = """
                + [0] 5-1
                  + [0] 5-1-1
                  + [1] 5-1-2
                  + [2] 5-1-3
                + [1] 5-2
                  + [0] 5-2-1
                  + [1] 5-2-2
                  + [2] 5-2-3
                    + [0] 5-2-3-1
                + [2] 5-3
                """;
        assertThat(output).isEqualTo(tmp);
        assertThat(countMenuItems(data.getMenu())).isEqualTo(10);
    }

    private int countMenuItems(Collection<UserWorkspaceMenuItemDTO> menuItemDTOS) {
        int count = 0;
        for (UserWorkspaceMenuItemDTO item : menuItemDTOS) {
            count++;
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                count += countMenuItems(item.getChildren());
            }
        }

        return count;
    }

    private String print(List<UserWorkspaceMenuItemDTO> items, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (UserWorkspaceMenuItemDTO m : items) {
            sb.append(prefix).append("+ [").append(m.getPosition()).append("] ").append(m.getName()).append("\n");
            sb.append(print(m.getChildren(), prefix + "  "));
        }
        return sb.toString();
    }
}
