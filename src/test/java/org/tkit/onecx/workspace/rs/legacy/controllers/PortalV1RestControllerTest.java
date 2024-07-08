package org.tkit.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.workspace.test.AbstractTest;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuItemDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.MenuRegistrationResponseDTO;
import gen.org.tkit.onecx.workspace.rs.legacy.model.ScopeDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(PortalV1RestController.class)
class PortalV1RestControllerTest extends AbstractTest {

    @Test
    void getMenuStructureForNoPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "LEGAGY_WRONG_PORTAL_ID")
                .get("{portalId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        assertThat(data).isNotNull().isEmpty();
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "test01")
                .get("{portalId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameAndAppTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalId", "test01")
                .pathParam("applicationId", "test01")
                .get("{applicationId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<MenuItemDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void submitMenuRegistrationRequestTest() {
        var request = new MenuRegistrationRequestDTO();
        request.setRequestVersion(0);
        var menuItems = new ArrayList<MenuItemDTO>();
        request.setMenuItems(menuItems);
        var menuItem = new MenuItemDTO();
        menuItems.add(menuItem);

        menuItem.setKey("PARAMETERS_MANAGEMENT_UI_ROOT");
        menuItem.setName("Parameters management root menu item");
        menuItem.setUrl("/1000kit-parameters-ui");
        menuItem.setDisabled(false);
        menuItem.setPosition(0);
        menuItem.setScope(ScopeDTO.APP);
        menuItem.setParentKey("PORTAL_MAIN_MENU");
        menuItem.setPermissionObject("PARAMETERS_MANAGEMENT_UI_ROOT");
        menuItem.setI18n(Map.of("de", "Parameters management", "en", "Parameters management"));
        menuItem.setChildren(new ArrayList<>());

        var subMenuItem = new MenuItemDTO();
        menuItem.getChildren().add(subMenuItem);
        subMenuItem.setKey("PARAMETERS_MANAGEMENT_SEARCH");
        subMenuItem.setName("Parameters management search page menu item");
        subMenuItem.setUrl("/1000kit-parameters-ui/parameters/search");
        subMenuItem.setDisabled(false);
        subMenuItem.setPosition(0);
        subMenuItem.setScope(ScopeDTO.PAGE);
        subMenuItem.setPortalExit(false);
        subMenuItem.setParentKey("PARAMETERS_MANAGEMENT_UI_ROOT");
        subMenuItem.setPermissionObject("PARAMETER_SEARCH");
        subMenuItem.setI18n(Map.of("de", "Parameters Management Search", "en", "Parameters Management Search"));

        var response = given()
                .contentType(APPLICATION_JSON)
                .body(request)
                .pathParam("portalId", "test03")
                .pathParam("appId", "parameters-management-ui")
                .post("{appId}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(MenuRegistrationResponseDTO.class);

        assertThat(response).isNotNull();
        assertThat(response.getApplied()).isFalse();
        assertThat(response.getNotice()).isEqualTo("TKITPORTAL10003 Menu registration request has been ignored");
    }

}
