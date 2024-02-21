package org.tkit.onecx.workspace.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuInternalRestController.class)
@WithDBData(value = "data/testdata-parent-change.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class MenuInternalRestControllerParentChangeTest extends AbstractTest {

    @Test
    void updateMenuItemParentTest() {

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "4")
                .get("tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceMenuItemStructureDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getMenuItems()).isNotNull().isNotEmpty();
        print(dto.getMenuItems(), "");

        // 4-2-3 pos 2 -> change parent to 4-1 pos 1
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId("4-1")
                .position(1)
                .modificationCount(0);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "4")
                .pathParam("menuItemId", "4-2-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(OK.getStatusCode());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "4")
                .get("tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceMenuItemStructureDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getMenuItems()).isNotNull().isNotEmpty();
        print(dto.getMenuItems(), "");

        // 4-1 -> 4-1-2 pos 2
        var parent = dto.getMenuItems().stream().filter(x -> x.getId().equals("4-1")).findFirst();
        assertThat(parent).isPresent();
        assertThat(parent.get().getPosition()).isEqualTo(0);

        var item = parent.get().getChildren().stream().filter(x -> x.getId().equals("4-2-2")).findFirst();
        assertThat(item).isPresent();
        assertThat(item.get().getPosition()).isEqualTo(1);
    }

    @Test
    void updateMenuItemParentToNullTest() {

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "4")
                .get("tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceMenuItemStructureDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getMenuItems()).isNotNull().isNotEmpty();
        print(dto.getMenuItems(), "");

        // 4-2-3 pos 2 -> change parent to null pos 0
        var request = new UpdateMenuItemParentRequestDTO()
                .parentItemId(null)
                .position(0)
                .modificationCount(0);

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("id", "4")
                .pathParam("menuItemId", "4-2-2")
                .put("{menuItemId}/parentItemId")
                .then().statusCode(OK.getStatusCode());

        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "4")
                .get("tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(WorkspaceMenuItemStructureDTO.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getMenuItems()).isNotNull().isNotEmpty();
        print(dto.getMenuItems(), "");

        // null<root> -> 4-1-2 pos 2
        var item = dto.getMenuItems().stream().filter(x -> x.getId().equals("4-2-2")).findFirst();
        assertThat(item).isPresent();
        assertThat(item.get().getPosition()).isEqualTo(0);
    }

    private void print(List<WorkspaceMenuItemDTO> items, String prefix) {
        for (WorkspaceMenuItemDTO m : items) {
            System.out.println(prefix + "+ [" + m.getPosition() + "] " + m.getId());
            print(m.getChildren(), prefix + "  ");
        }
    }
}
