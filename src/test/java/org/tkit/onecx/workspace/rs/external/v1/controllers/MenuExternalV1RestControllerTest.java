package org.tkit.onecx.workspace.rs.external.v1.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MenuExternalV1RestController.class)
@WithDBData(value = "data/testdata-external.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class MenuExternalV1RestControllerTest extends AbstractTest {
    @Test
    void getMenuItemsForWorkspaceIdTest() {
        var criteria = new MenuItemSearchCriteriaDTO()
                .workspaceId("11-111");

        var dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(3);

        criteria.setWorkspaceId("");
        dto = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().as(MenuItemPageResultDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getStream()).isNotNull().isNotEmpty().hasSize(6);
    }

    @Test
    void getMenuStructureForWorkspaceIdTest() {
        var criteria = new MenuStructureSearchCriteriaDTO().workspaceId("11-111");

        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/tree")
                .then()
                .statusCode(OK.getStatusCode())
                .extract().body().as(MenuItemStructureDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getMenuItems()).hasSize(1);
        assertThat(countMenuItems(data.getMenuItems())).isEqualTo(3);
    }

    private int countMenuItems(Collection<WorkspaceMenuItemDTO> menuItemDTOS) {
        int count = 0;
        for (WorkspaceMenuItemDTO item : menuItemDTOS) {
            count++;
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                count += countMenuItems(item.getChildren());
            }
        }

        return count;
    }

    @Test
    void failToGetMenuItemsWithoutWorkspaceId() {
        var criteria = new MenuItemSearchCriteriaDTO();
        var data = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().body().as(ProblemDetailResponseDTO.class);

        assertThat(data.getErrorCode()).isEqualTo("CONSTRAINT_VIOLATIONS");
        assertThat(data.getDetail())
                .isEqualTo("searchMenuItemsByCriteriaV1.menuItemSearchCriteriaDTOV1.workspaceId: must not be null");
    }
}
