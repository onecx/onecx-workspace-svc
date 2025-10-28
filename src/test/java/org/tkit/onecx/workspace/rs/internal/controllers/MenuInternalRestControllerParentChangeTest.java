package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-parent-change.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-ws:all", "ocx-ws:read", "ocx-ws:write", "ocx-ws:delete" })
class MenuInternalRestControllerParentChangeTest extends AbstractTest {

    private static Stream<Arguments> menuPosition() {
        return Stream.of(
                arguments(3, 3),
                arguments(30, 3),
                arguments(1, 2));
    }

    @ParameterizedTest
    @MethodSource("menuPosition")
    void updateMenuItemLastPositionTest(int position, int result) {

        printWorkspace();

        // 4-2-3 pos 2 -> change parent to 4-1 pos 1
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("4-1")
                .position(position)
                .modificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "4-2-2")
                .put("{menuItemId}/reorder")
                .then().statusCode(OK.getStatusCode());

        var dto = printWorkspace();

        // 4-1 -> 4-1-2 pos 2
        var parent = dto.getMenuItems().stream().filter(x -> x.getId().equals("4-1")).findFirst();
        assertThat(parent).isPresent();
        assertThat(parent.get().getPosition()).isZero();

        var item = parent.get().getChildren().stream().filter(x -> x.getId().equals("4-2-2")).findFirst();
        assertThat(item).isPresent();
        assertThat(item.get().getPosition()).isEqualTo(result);
    }

    @Test
    void updateMenuItemParentNullToNullTest() {

        // 4-2-3 pos 2 -> change parent to null pos 0
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId(null)
                .position(0)
                .modificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "4-3")
                .put("{menuItemId}/reorder")
                .then().statusCode(OK.getStatusCode());
    }

    @Test
    void updateMenuItemParentNoChangeTest() {

        // 4-2-3 pos 2 -> change parent to null pos 0
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("4-1")
                .position(1)
                .modificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "4-1-2")
                .put("{menuItemId}/reorder")
                .then().statusCode(OK.getStatusCode());
    }

    @Test
    void updateMenuItemParentToNullTest() {

        printWorkspace();

        // 4-2-3 pos 2 -> change parent to null pos 0
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId(null)
                .position(0)
                .modificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "4-2-2")
                .put("{menuItemId}/reorder")
                .then().statusCode(OK.getStatusCode());

        var dto = printWorkspace();

        // null<root> -> 4-1-2 pos 2
        var item = dto.getMenuItems().stream().filter(x -> x.getId().equals("4-2-2")).findFirst();
        assertThat(item).isPresent();
        assertThat(item.get().getPosition()).isZero();
    }

    @Test
    void updateMenuItemParentToMinusPositionTest() {

        printWorkspace();

        // 4-2-3 pos 2 -> change parent to null pos 0
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId(null)
                .position(-10)
                .modificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("menuItemId", "4-2-2")
                .put("{menuItemId}/reorder")
                .then().statusCode(BAD_REQUEST.getStatusCode());
    }

    public MenuItemStructureDTO printWorkspace() {

        var criteria = new MenuStructureSearchCriteriaDTO().workspaceId("4");

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemStructureDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getMenuItems()).isNotNull().isNotEmpty();
        print(dto.getMenuItems(), "");
        return dto;
    }

    private void print(List<WorkspaceMenuItemDTO> items, String prefix) {
        for (WorkspaceMenuItemDTO m : items) {
            System.out.println(prefix + "+ [" + m.getPosition() + "] " + m.getId());
            print(m.getChildren(), prefix + "  ");
        }
    }
}
