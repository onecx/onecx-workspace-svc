package io.github.onecx.workspace.rs.legacy.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.workspace.rs.legacy.model.MenuRegistrationRequestDTO;
import gen.io.github.onecx.workspace.rs.legacy.model.MenuRegistrationResponseDTO;
import gen.io.github.onecx.workspace.rs.legacy.model.ScopeDTO;
import gen.io.github.onecx.workspace.rs.legacy.model.TkitMenuItemStructureDTO;
import io.github.onecx.workspace.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
@TestHTTPEndpoint(TkitPortalRestController.class)
class TkitPortalRestControllerTenantTest extends AbstractTest {

    @Test
    void getMenuStructureForPortalNameOrg3Test() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .header(APM_HEADER_PARAM, createToken("org3"))
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<TkitMenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isEmpty();
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void getMenuStructureForPortalNameTest() {

        var data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<TkitMenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);

        data = given()
                .contentType(APPLICATION_JSON)
                .pathParam("portalName", "test01")
                .header(APM_HEADER_PARAM, createToken("org1"))
                .queryParam("interpolate", Boolean.FALSE)
                .get()
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(new TypeRef<List<TkitMenuItemStructureDTO>>() {
                });

        assertThat(data).isNotNull().isNotEmpty().hasSize(5);
    }

    @Test
    @WithDBData(value = "data/testdata-legacy.xml", deleteAfterTest = true, deleteBeforeInsert = true)
    void submitMenuRegistrationRequestTest() {
        var request = new MenuRegistrationRequestDTO();
        request.setRequestVersion(0);
        var menuItems = new ArrayList<TkitMenuItemStructureDTO>();
        request.setMenuItems(menuItems);
        var menuItem = new TkitMenuItemStructureDTO();
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

        var subMenuItem = new TkitMenuItemStructureDTO();
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
                .pathParam("portalName", "test03")
                .pathParam("appId", "parameters-management-ui")
                .header(APM_HEADER_PARAM, createToken("org2"))
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
